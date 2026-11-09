package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveCommentsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<Comment>> = eventRepository.comments
}
