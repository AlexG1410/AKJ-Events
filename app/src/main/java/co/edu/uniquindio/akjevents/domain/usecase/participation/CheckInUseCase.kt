package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.CheckInRegistration
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import javax.inject.Inject

sealed interface CheckInResult {
    data class Success(val event: CommunityEvent, val pointsEarned: Int) : CheckInResult
    data class AlreadyCheckedIn(val event: CommunityEvent) : CheckInResult
    data class Full(val event: CommunityEvent) : CheckInResult
    data class OwnEvent(val event: CommunityEvent) : CheckInResult
    data object InvalidCode : CheckInResult
}

/** Valida el código QR de un evento, registra la asistencia presencial y suma los puntos. */
class CheckInUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val awardPoints: AwardPointsUseCase,
    private val awardBadge: AwardBadgeUseCase
) {
    suspend operator fun invoke(code: String, userId: String): CheckInResult {
        val event = eventRepository.findPublicEventByCheckInCode(code.trim().uppercase()) ?: return CheckInResult.InvalidCode
        if (event.organizerId == userId) return CheckInResult.OwnEvent(event)
        return when (eventRepository.registerCheckIn(event.id, userId)) {
            CheckInRegistration.OWN_EVENT -> CheckInResult.OwnEvent(event)
            CheckInRegistration.ALREADY_CHECKED_IN -> CheckInResult.AlreadyCheckedIn(event)
            CheckInRegistration.FULL -> CheckInResult.Full(event)
            CheckInRegistration.NOT_FOUND -> CheckInResult.InvalidCode
            CheckInRegistration.REGISTERED -> {
                awardPoints(userId, ReputationPoints.CHECK_IN, PointsReason.CHECK_IN, event.title)
                val checkIns = eventRepository.attendances.value.count { it.userId == userId && it.checkedIn }
                if (checkIns == 1) awardBadge(userId, Badge.FIRST_CHECK_IN)
                CheckInResult.Success(eventRepository.findEvent(event.id) ?: event, ReputationPoints.CHECK_IN)
            }
        }
    }
}
