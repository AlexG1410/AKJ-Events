package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject

/** Solo el organizador edita su evento, que vuelve a revisión. Un evento finalizado ya no se edita. */
class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: CommunityEvent, actorId: String): Boolean {
        val existing = eventRepository.findEvent(event.id) ?: return false
        if (existing.organizerId != actorId || existing.status == EventStatus.FINISHED) return false
        // El cupo no puede quedar por debajo de los asistentes ya confirmados
        if (event.capacity != null && event.capacity < existing.attendanceCount) return false
        return eventRepository.updateEvent(event.copy(status = EventStatus.PENDING, rejectionReason = null)) != null
    }
}
