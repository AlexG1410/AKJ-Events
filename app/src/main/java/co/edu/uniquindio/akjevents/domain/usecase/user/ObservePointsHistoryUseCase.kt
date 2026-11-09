package co.edu.uniquindio.akjevents.domain.usecase.user

import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class ObservePointsHistoryUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): StateFlow<List<PointsEntry>> = userRepository.pointsHistory
}
