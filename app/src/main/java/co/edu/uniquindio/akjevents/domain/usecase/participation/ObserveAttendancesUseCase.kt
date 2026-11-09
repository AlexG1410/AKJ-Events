package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObserveAttendancesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): StateFlow<List<Attendance>> = eventRepository.attendances
}
