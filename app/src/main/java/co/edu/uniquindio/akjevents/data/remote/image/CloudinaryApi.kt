package co.edu.uniquindio.akjevents.data.remote.image

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * API de subida de Cloudinary (guía 18). Usa un preset sin firma, así la app no guarda ninguna clave
 * secreta: el preset define qué se permite subir.
 */
interface CloudinaryApi {
    @Multipart
    @POST("v1_1/{cloudName}/image/upload")
    suspend fun upload(
        @Path("cloudName") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody,
        @Part("folder") folder: RequestBody
    ): CloudinaryUploadDto
}

/** Respuesta de Cloudinary; solo interesa la URL segura (https) de la imagen subida. */
@Keep
data class CloudinaryUploadDto(
    @SerializedName("secure_url") val secureUrl: String = "",
    @SerializedName("public_id") val publicId: String = ""
)
