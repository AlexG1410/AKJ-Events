package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import javax.inject.Inject

/** Publica un evento nuevo, siempre pendiente de verificación, y premia al organizador. */
class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val awardPoints: AwardPointsUseCase,
    private val awardBadge: AwardBadgeUseCase
) {
    suspend operator fun invoke(event: CommunityEvent) {
        // Todo evento nuevo espera la revisión de un moderador antes de ser público
        eventRepository.createEvent(event.copy(status = EventStatus.PENDING, rejectionReason = null))
        awardPoints(event.organizerId, ReputationPoints.CREATE_EVENT, PointsReason.CREATE_EVENT, event.title)
        if (eventRepository.findEventsByOrganizer(event.organizerId).size == 1) {
            awardBadge(event.organizerId, Badge.FIRST_EVENT)
        }
    }
}
