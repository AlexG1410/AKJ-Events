package co.edu.uniquindio.akjevents.domain.usecase.notification

import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Todas las notificaciones; cada pantalla filtra las del usuario de la sesión. */
class ObserveNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): StateFlow<List<AppNotification>> = notificationRepository.notifications
}
