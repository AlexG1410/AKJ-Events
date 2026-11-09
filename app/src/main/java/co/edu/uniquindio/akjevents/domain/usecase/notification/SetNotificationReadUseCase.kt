package co.edu.uniquindio.akjevents.domain.usecase.notification

import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import javax.inject.Inject

class SetNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(notificationId: String, isRead: Boolean) = notificationRepository.setRead(notificationId, isRead)
}
