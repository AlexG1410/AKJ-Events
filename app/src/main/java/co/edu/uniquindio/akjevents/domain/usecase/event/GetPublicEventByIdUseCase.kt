package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject

class GetPublicEventByIdUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(id: String): CommunityEvent? = eventRepository.findPublicEvent(id)
}
