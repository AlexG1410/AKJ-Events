package co.edu.uniquindio.akjevents.domain.usecase.moderation

import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Rechazar exige un motivo, que se le notifica al organizador. El evento rechazado no es público. */
class RejectEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(eventId: String, moderatorId: String, reason: String): Boolean {
        val event = eventRepository.findEvent(eventId) ?: return false
        val isModerator = userRepository.findById(moderatorId)?.isModerator == true
        if (event.status != EventStatus.PENDING || reason.isBlank() || !isModerator) return false
        val rejected = eventRepository.updateStatus(eventId, EventStatus.REJECTED, reason.trim()) ?: return false
        notificationRepository.notify(
            userId = event.organizerId,
            type = NotificationType.EVENT_REJECTED,
            title = "Revisión requerida: ${event.title}",
            message = "Motivo: ${rejected.rejectionReason}",
            eventId = eventId
        )
        return true
    }
}
