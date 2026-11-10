package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject

/** El organizador no se inscribe como participante ni consume un cupo en su propio evento. */
class ConfirmAttendanceUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): AttendanceResult {
        val event = eventRepository.findEvent(eventId) ?: return AttendanceResult.NOT_FOUND
        if (event.status == EventStatus.FINISHED) return AttendanceResult.CLOSED
        if (event.status != EventStatus.VERIFIED) return AttendanceResult.NOT_FOUND
        if (event.organizerId == userId) return AttendanceResult.OWN_EVENT
        // El repositorio revalida el evento y el cupo dentro de la operación atómica.
        return eventRepository.confirmAttendance(eventId, userId)
    }
}
