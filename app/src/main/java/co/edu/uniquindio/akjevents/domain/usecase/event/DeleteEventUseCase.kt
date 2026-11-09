package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Lo puede borrar su organizador o un moderador. */
class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(eventId: String, actorId: String): Boolean {
        val event = eventRepository.findEvent(eventId) ?: return false
        val isModerator = userRepository.findById(actorId)?.isModerator == true
        if (event.organizerId != actorId && !isModerator) return false
        return eventRepository.deleteEvent(eventId)
    }
}
