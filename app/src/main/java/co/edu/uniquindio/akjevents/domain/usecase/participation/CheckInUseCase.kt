package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.CheckInRegistration
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.repository.CheckInRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

sealed interface CheckInResult {
    data class Success(val event: CommunityEvent, val pointsEarned: Int, val pointsBefore: Int? = null) : CheckInResult
    data class AlreadyCheckedIn(val event: CommunityEvent) : CheckInResult
    data class Full(val event: CommunityEvent) : CheckInResult
    data class OwnEvent(val event: CommunityEvent) : CheckInResult
    data object WrongEvent : CheckInResult
    data object InvalidCode : CheckInResult
}

/** Valida el código QR de un evento, registra la asistencia presencial y suma los puntos. */
class CheckInUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val completeCheckIn: CheckInRepository,
    private val awardBadge: AwardBadgeUseCase,
    private val notifications: NotificationRepository
) {
    suspend operator fun invoke(code: String, userId: String, expectedEventId: String? = null): CheckInResult {
        val event = eventRepository.findPublicEventByCheckInCode(code.trim().uppercase()) ?: return CheckInResult.InvalidCode
        if (expectedEventId != null && event.id != expectedEventId) return CheckInResult.WrongEvent
        if (event.organizerId == userId) return CheckInResult.OwnEvent(event)
        val commit = completeCheckIn.complete(event.id, userId, ReputationPoints.CHECK_IN)
        return when (commit.registration) {
            CheckInRegistration.OWN_EVENT -> CheckInResult.OwnEvent(event)
            CheckInRegistration.ALREADY_CHECKED_IN -> CheckInResult.AlreadyCheckedIn(event)
            CheckInRegistration.FULL -> CheckInResult.Full(event)
            CheckInRegistration.NOT_FOUND -> CheckInResult.InvalidCode
            CheckInRegistration.REGISTERED -> {
                // Insignias/avisos son secundarios: si fallan, el check-in y su premio ya están confirmados.
                try {
                    if (commit.pointsEarned > 0) {
                        awardBadge(userId, Badge.FIRST_CHECK_IN)
                        val before = commit.before
                        val after = commit.after
                        if (before != null && after != null && after.level.ordinal > before.level.ordinal) {
                            notifications.notify(userId = userId, type = NotificationType.LEVEL_UP, level = after.level)
                        }
                    }
                } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { /* No repetir el premio por un fallo del aviso. */ }
                CheckInResult.Success(commit.event ?: event, commit.pointsEarned, commit.before?.points)
            }
        }
    }
}
