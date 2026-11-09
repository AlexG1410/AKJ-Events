package co.edu.uniquindio.akjevents.domain.usecase.recommendation

import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Emite cada vez que cambian los datos que afectan las recomendaciones. */
class ObserveRecommendationChangesUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(): Flow<Unit> = recommendationRepository.changes
}
