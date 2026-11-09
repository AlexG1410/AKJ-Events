package co.edu.uniquindio.akjevents.domain.repository

/** Carpeta del servicio de imágenes donde se guarda cada tipo de foto. */
enum class ImageFolder(val path: String) {
    EVENTS("akj-events/events"),
    PROFILES("akj-events/profiles")
}

/** Sube a la nube las fotos que el usuario toma con la cámara o elige de la galería. */
interface ImageRepository {
    /**
     * Sube la imagen del [localUri] (un `content://` de la galería o de la cámara) y devuelve su URL pública.
     * Lanza una excepción si no se pudo leer o subir.
     */
    suspend fun upload(localUri: String, folder: ImageFolder): String
}
