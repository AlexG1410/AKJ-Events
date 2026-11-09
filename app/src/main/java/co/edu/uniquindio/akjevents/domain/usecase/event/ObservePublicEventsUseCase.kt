package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Eventos verificados: los únicos que aparecen en el feed y en el mapa. */
class ObservePublicEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<CommunityEvent>> = eventRepository.publicEvents
}
