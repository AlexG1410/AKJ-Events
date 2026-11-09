package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import javax.inject.Inject

/**
 * Marca el evento como finalizado: lo puede hacer su organizador o cualquier moderador.
 * Si era público, el organizador gana puntos y los asistentes reciben un aviso.
 */
class FinishEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val awardPoints: AwardPointsUseCase
) {
    suspend operator fun invoke(eventId: String, actorId: String): Boolean {
        val event = eventRepository.findEvent(eventId) ?: return false
        val canFinish = event.organizerId == actorId || userRepository.findById(actorId)?.isModerator == true
        if (!canFinish || event.status == EventStatus.FINISHED) return false
        val wasPublic = event.status == EventStatus.VERIFIED
        eventRepository.updateStatus(eventId, EventStatus.FINISHED) ?: return false

        if (wasPublic) {
            awardPoints(event.organizerId, ReputationPoints.EVENT_FINISHED, PointsReason.EVENT_FINISHED, event.title)
            eventRepository.attendances.value.filter { it.eventId == eventId && it.userId != actorId }.forEach {
                notificationRepository.notify(
                    userId = it.userId,
                    type = NotificationType.EVENT_FINISHED,
                    eventId = eventId,
                    eventTitle = event.title
                )
            }
        }
        if (actorId != event.organizerId) {
            notificationRepository.notify(
                userId = event.organizerId,
                type = NotificationType.EVENT_FINISHED_BY_MODERATOR,
                eventId = eventId,
                eventTitle = event.title
            )
        }
        return true
    }
}
