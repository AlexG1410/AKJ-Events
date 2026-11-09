package co.edu.uniquindio.akjevents.data.repository.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import androidx.core.net.toUri
import co.edu.uniquindio.akjevents.BuildConfig
import co.edu.uniquindio.akjevents.data.remote.image.CloudinaryApi
import co.edu.uniquindio.akjevents.domain.repository.ImageFolder
import co.edu.uniquindio.akjevents.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Sube las fotos a Cloudinary. Antes de subirlas las reduce a [MAX_SIDE_PX] por lado y las comprime en JPEG,
 * para que una foto de la cámara (varios MB) no tarde ni gaste datos de más. `ImageDecoder` ya respeta la
 * orientación con la que se tomó la foto.
 */
@Singleton
class CloudinaryImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: CloudinaryApi
) : ImageRepository {

    override suspend fun upload(localUri: String, folder: ImageFolder): String {
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
        check(cloudName.isNotBlank() && uploadPreset.isNotBlank()) {
            "Falta configurar cloudinary.cloudName y cloudinary.uploadPreset en local.properties"
        }
        val jpeg = withContext(Dispatchers.IO) { compress(localUri) }
        val file = MultipartBody.Part.createFormData("file", "foto.jpg", jpeg.toRequestBody(JPEG))
        val response = api.upload(
            cloudName = cloudName,
            file = file,
            uploadPreset = uploadPreset.toRequestBody(TEXT),
            folder = folder.path.toRequestBody(TEXT)
        )
        check(response.secureUrl.isNotBlank()) { "Cloudinary no devolvió la URL de la imagen" }
        return response.secureUrl
    }

    private fun compress(localUri: String): ByteArray {
        val source = ImageDecoder.createSource(context.contentResolver, localUri.toUri())
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val largestSide = max(info.size.width, info.size.height)
            if (largestSide > MAX_SIDE_PX) {
                val scale = MAX_SIDE_PX.toFloat() / largestSide
                decoder.setTargetSize((info.size.width * scale).toInt(), (info.size.height * scale).toInt())
            }
            // Mapa de bits en memoria normal: se puede comprimir sin copiarlo
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            bitmap.recycle()
            output.toByteArray()
        }
    }

    private companion object {
        const val MAX_SIDE_PX = 1600
        const val JPEG_QUALITY = 85
        val JPEG = "image/jpeg".toMediaType()
        val TEXT = "text/plain".toMediaType()
    }
}
