package co.edu.uniquindio.akjevents.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Crea un archivo vacío en la caché y devuelve su URI a través del FileProvider (guía 15), listo para
 * entregárselo a la app de cámara, que guarda ahí la foto.
 */
fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
    // La autoridad debe coincidir con la declarada en el manifest
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}
