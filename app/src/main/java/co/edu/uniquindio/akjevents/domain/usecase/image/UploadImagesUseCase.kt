package co.edu.uniquindio.akjevents.domain.usecase.image

import co.edu.uniquindio.akjevents.domain.repository.ImageFolder
import co.edu.uniquindio.akjevents.domain.repository.ImageRepository
import javax.inject.Inject

/**
 * Sube las fotos nuevas y devuelve la lista de URLs en el mismo orden. Las que ya son URLs públicas
 * (por ejemplo, las de un evento que se está editando) se conservan sin volver a subirlas.
 */
class UploadImagesUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(images: List<String>, folder: ImageFolder): List<String> =
        images.map { image -> if (isRemote(image)) image else imageRepository.upload(image, folder) }

    companion object {
        /** Una imagen ya subida tiene una URL web; las nuevas son URIs locales (`content://`). */
        fun isRemote(image: String): Boolean = image.startsWith("https://") || image.startsWith("http://")
    }
}
