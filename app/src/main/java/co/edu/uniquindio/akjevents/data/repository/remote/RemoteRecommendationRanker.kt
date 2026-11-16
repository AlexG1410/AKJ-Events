package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.remote.ai.*
import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

/** Solo datos mínimos de eventos. No serializar RecommendationCandidate/User directamente. */
class RemoteRecommendationRanker @Inject constructor(
    private val api: RecommendationApi,
    private val tokens: AiSessionTokenProvider,
    private val config: AiServiceConfig
) : RecommendationRanker {
    override val usesRemoteService: Boolean = true

    override suspend fun rank(request: RecommendationRequest): List<RankedRecommendation> {
        check(config.isConfigured) { "AI_NOT_CONFIGURED" }
        require(request.candidates.size <= 24 && request.interactions.size <= 20)
        if (request.candidates.isEmpty()) return emptyList()
        val payload = RecommendationRequestDto(
            interests = request.interests.map { it.name }.sorted(),
            interactions = request.interactions.map { RecommendationInteractionDto(it.event.title.take(100), it.event.description.take(200), it.event.category.name, it.checkedIn) },
            candidates = request.candidates.map { RecommendationCandidateDto(it.event.id, it.event.title.take(100), it.event.description.take(200), it.event.category.name) },
            language = request.language
        )
        return withTimeoutOrNull(22_000) {
            val response = api.recommend("Bearer ${tokens.token()}", payload)
            val items = requireNotNull(response.items) { "AI_INVALID_RANKING" }
            val ids = request.candidates.map { it.event.id }.toSet()
            require(ids.size == request.candidates.size && items.size == ids.size)
            val seen = mutableSetOf<String>()
            items.map { item ->
                val entry = requireNotNull(item)
                val id = requireNotNull(entry.eventId)
                val reason = requireNotNull(entry.reason)
                require(id in ids && seen.add(id) && reason.isNotBlank() && reason.length <= 180 && reason.none(Char::isISOControl))
                RankedRecommendation(id, reason.trim())
            }
        } ?: error("AI_TIMEOUT")
        // Las excepciones activan respaldo en el repositorio; CancellationException se propaga.
    }
}
