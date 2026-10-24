package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import kotlinx.coroutines.flow.Flow

/** Un futuro adaptador llamará a una función del servidor, nunca al modelo con una clave en Android. */
fun interface RecommendationRanker {
    suspend fun rank(request: RecommendationRequest): List<RankedRecommendation>
}

interface RecommendationRepository {
    val changes: Flow<Unit>
    suspend fun recommend(): RecommendationFeed
    fun revalidate(feed: RecommendationFeed): RecommendationFeed
}
