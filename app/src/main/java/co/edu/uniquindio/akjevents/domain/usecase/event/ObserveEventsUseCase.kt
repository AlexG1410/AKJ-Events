package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Todos los eventos, en cualquier estado; para el organizador y el moderador. */
class ObserveEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<CommunityEvent>> = eventRepository.events
}
