package co.edu.uniquindio.akjevents.domain.usecase.moderation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import javax.inject.Inject

/** El moderador aprueba un evento pendiente: se hace público y se le avisa al organizador. */
class VerifyEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val awardPoints: AwardPointsUseCase,
    private val awardBadge: AwardBadgeUseCase
) {
    operator fun invoke(eventId: String, moderatorId: String): Boolean {
        val event = eventRepository.findEvent(eventId) ?: return false
        val isModerator = userRepository.findById(moderatorId)?.isModerator == true
        if (event.status != EventStatus.PENDING || !isModerator) return false
        eventRepository.updateStatus(eventId, EventStatus.VERIFIED) ?: return false

        // Una aprobación solo da puntos una vez por evento, aunque se edite y vuelva a revisión
        if (eventRepository.registerVerificationReward(eventId)) {
            awardPoints(event.organizerId, ReputationPoints.EVENT_VERIFIED, "«${event.title}» fue verificado")
        }
        notificationRepository.notify(
            userId = event.organizerId,
            type = NotificationType.EVENT_VERIFIED,
            title = "¡Tu evento ha sido aprobado y publicado!",
            message = "«${event.title}» superó la revisión y ya es visible para la comunidad.",
            eventId = eventId
        )
        val verifiedCount = eventRepository.findEventsByOrganizer(event.organizerId).count {
            it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED
        }
        if (verifiedCount >= ReputationPoints.VERIFIED_FOR_BADGE) awardBadge(event.organizerId, Badge.TEN_VERIFIED)
        return true
    }
}
