package co.edu.uniquindio.akjevents.domain.usecase.recommendation

import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import javax.inject.Inject

/** Quita de una lista ya calculada los eventos que dejaron de ser recomendables. */
class RevalidateRecommendationsUseCase @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) {
    operator fun invoke(feed: RecommendationFeed): RecommendationFeed = recommendationRepository.revalidate(feed)
}
