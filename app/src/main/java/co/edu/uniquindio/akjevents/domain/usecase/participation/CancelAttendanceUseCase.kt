package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject

/** Cancela la asistencia; no se puede después de hacer check-in. */
class CancelAttendanceUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(eventId: String, userId: String): Boolean = eventRepository.cancelAttendance(eventId, userId)
}
