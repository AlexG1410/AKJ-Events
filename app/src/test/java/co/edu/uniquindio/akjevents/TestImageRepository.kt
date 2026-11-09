package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.domain.repository.ImageFolder
import co.edu.uniquindio.akjevents.domain.repository.ImageRepository
import java.io.IOException

/** Servicio de imágenes de prueba: no sube nada, devuelve una URL inventada y recuerda qué se subió. */
class TestImageRepository : ImageRepository {
    val uploaded = mutableListOf<String>()

    /** Si es true, las subidas fallan como si no hubiera conexión. */
    var failUploads = false

    override suspend fun upload(localUri: String, folder: ImageFolder): String {
        if (failUploads) throw IOException("Sin conexión")
        uploaded += localUri
        return "https://res.cloudinary.test/${folder.path}/${uploaded.size}.jpg"
    }
}
