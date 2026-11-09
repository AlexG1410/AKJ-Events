package co.edu.uniquindio.akjevents.domain.usecase.notification

import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String) = notificationRepository.markAllAsRead(userId)
}
