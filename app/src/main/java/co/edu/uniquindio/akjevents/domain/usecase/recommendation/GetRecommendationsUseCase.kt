package co.edu.uniquindio.akjevents.domain.usecase.recommendation

import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import javax.inject.Inject

/** Eventos recomendados para el usuario de la sesión ("Recomendados para ti"). */
class GetRecommendationsUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    suspend operator fun invoke(): RecommendationFeed =
        recommendationRepository.revalidate(recommendationRepository.recommend())
}
