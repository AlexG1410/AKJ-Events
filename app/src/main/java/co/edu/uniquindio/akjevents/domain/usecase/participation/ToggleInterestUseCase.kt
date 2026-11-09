package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import java.time.YearMonth
import javax.inject.Inject

/**
 * Marca o desmarca "Me interesa". El organizador gana (o pierde) puntos por cada voto, y el evento
 * más votado del mes le da una insignia. Devuelve el nuevo estado, o null si el evento no es público.
 */
class ToggleInterestUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val awardPoints: AwardPointsUseCase,
    private val awardBadge: AwardBadgeUseCase
) {
    operator fun invoke(eventId: String, userId: String): Boolean? {
        val interested = eventRepository.toggleInterest(eventId, userId) ?: return null
        val event = eventRepository.findPublicEvent(eventId) ?: return interested
        // Votar por un evento propio no da puntos
        if (event.organizerId != userId) {
            val points = if (interested) ReputationPoints.INTEREST_RECEIVED else -ReputationPoints.INTEREST_RECEIVED
            val reason = if (interested) "Recibiste un «Me interesa» en «${event.title}»" else "Retiraron un «Me interesa» en «${event.title}»"
            awardPoints(event.organizerId, points, reason)
        }
        if (interested && isMostRelevantOfItsMonth(event)) awardBadge(event.organizerId, Badge.FEATURED_OF_MONTH)
        return interested
    }

    private fun isMostRelevantOfItsMonth(event: CommunityEvent): Boolean {
        val month = YearMonth.from(event.startsAt)
        val top = eventRepository.publicEvents.value
            .filter { YearMonth.from(it.startsAt) == month }
            .maxByOrNull { it.interestCount }
        return top?.id == event.id
    }
}
