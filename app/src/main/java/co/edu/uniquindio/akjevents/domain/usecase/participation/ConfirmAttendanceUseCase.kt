package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject

/** Confirma la asistencia a un evento público si queda cupo. */
class ConfirmAttendanceUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(eventId: String, userId: String): AttendanceResult = eventRepository.confirmAttendance(eventId, userId)
}
