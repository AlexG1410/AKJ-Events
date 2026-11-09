package co.edu.uniquindio.akjevents.domain.usecase.user

import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Borra la cuenta junto con sus eventos, comentarios, participación y notificaciones, y cierra la sesión. */
class DeleteAccountUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Boolean {
        if (userRepository.findById(userId) == null) return false
        eventRepository.removeUserData(userId)
        notificationRepository.deleteForUser(userId)
        return userRepository.deleteAccount(userId)
    }
}
