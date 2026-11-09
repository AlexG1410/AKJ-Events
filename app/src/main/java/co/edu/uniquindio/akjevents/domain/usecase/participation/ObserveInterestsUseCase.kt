package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveInterestsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<Interest>> = eventRepository.interests
}
