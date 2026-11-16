package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import kotlinx.coroutines.flow.Flow

/** El adaptador remoto llama al Worker; nunca contiene claves del proveedor. */
fun interface RecommendationRanker {
    suspend fun rank(request: RecommendationRequest): List<RankedRecommendation>
    val usesRemoteService: Boolean get() = false
}

interface RecommendationRepository {
    val changes: Flow<Unit>
    suspend fun recommend(): RecommendationFeed
    fun revalidate(feed: RecommendationFeed): RecommendationFeed
}
