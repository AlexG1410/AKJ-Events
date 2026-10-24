package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationCandidate
import co.edu.uniquindio.akjevents.domain.model.RecommendationEvent
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.model.RecommendationInteraction
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import co.edu.uniquindio.akjevents.domain.model.RecommendedEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime

/** Filtra antes de clasificar y valida de nuevo usando los datos actuales al recibir la respuesta. */
class LocalRecommendationRepository(
    private val events: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared,
    private val ranker: RecommendationRanker = LocalRecommendationRanker(),
    private val now: () -> LocalDateTime = LocalDateTime::now
) : RecommendationRepository {
    private val fallback = LocalRecommendationRanker()

    override val changes: Flow<Unit> = combine(
        events.publicEvents, events.events, events.interests, events.attendances, users.currentUser
    ) { _, _, _, _, _ -> }

    override suspend fun recommend(): RecommendationFeed {
        val user = users.currentUser.value ?: return RecommendationFeed()
        val candidates = eligibleEvents(user).map { candidate(it, user) }
        val liked = events.interests.value.filter { it.userId == user.id }.map { it.eventId }.toSet()
        val checkedIn = events.attendances.value.filter { it.userId == user.id && it.checkedIn }.map { it.eventId }.toSet()
        val history = events.events.value.filter {
            (it.id in liked || it.id in checkedIn) &&
                (it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED)
        }.sortedWith(compareByDescending<CommunityEvent> { it.startsAt }.thenBy { it.id })
            .take(MAX_HISTORY).map { RecommendationInteraction(it.brief(), it.id in checkedIn) }
        val fullRequest = RecommendationRequest(user.interests, history, candidates)
        // La preselección local mantiene pequeño el futuro envío al servidor.
        val shortlist = fallback.rank(fullRequest).take(MAX_CANDIDATES).map { it.eventId }.toSet()
        val request = fullRequest.copy(candidates = candidates.filter { it.event.id in shortlist })
        val local = fallback.rank(request)
        val response = try {
            ranker.rank(request).takeIf { validRanking(it, shortlist) } ?: local
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            local
        }
        if (users.currentUser.value?.id != user.id) return RecommendationFeed()
        val byId = eligibleEvents(user).associateBy { it.id }
        // Una respuesta que quedó obsoleta tampoco se acepta como clasificación válida.
        val ranked = if (response.any { it.eventId !in byId }) local else response
        val feed = RecommendationFeed(
            userId = user.id,
            items = ranked.mapNotNull { rankedEvent ->
                byId[rankedEvent.eventId]?.let { event ->
                    RecommendedEvent(event, rankedEvent.reason.trim(), user.location.distanceKmTo(event.location))
                }
            },
            hasPersonalization = user.interests.isNotEmpty() || history.isNotEmpty()
        )
        return revalidate(feed)
    }

    override fun revalidate(feed: RecommendationFeed): RecommendationFeed {
        val user = users.currentUser.value
        if (user == null || feed.userId != user.id) return RecommendationFeed()
        val byId = eligibleEvents(user).associateBy { it.id }
        return feed.copy(items = feed.items.distinctBy { it.event.id }.mapNotNull { item ->
            byId[item.event.id]?.let { item.copy(event = it, distanceKm = user.location.distanceKmTo(it.location)) }
        })
    }

    private fun eligibleEvents(user: User): List<CommunityEvent> {
        val currentTime = now()
        val confirmed = events.attendances.value.filter { it.userId == user.id }.map { it.eventId }.toSet()
        return events.publicEvents.value.filter {
            it.status == EventStatus.VERIFIED && it.startsAt.isAfter(currentTime) &&
                it.organizerId != user.id && it.id !in confirmed &&
                (it.capacity == null || it.attendanceCount < it.capacity)
        }.distinctBy { it.id }
    }

    private fun candidate(event: CommunityEvent, user: User) = RecommendationCandidate(
        event = event.brief(), startsAt = event.startsAt,
        distanceKm = user.location.distanceKmTo(event.location),
        remainingSpots = event.capacity?.let { (it - event.attendanceCount).coerceAtLeast(0) },
        availableRatio = event.capacity?.let { ((it - event.attendanceCount).toDouble() / it).coerceIn(0.0, 1.0) } ?: 1.0
    )

    private fun CommunityEvent.brief() = RecommendationEvent(id, title.take(100), description.take(500), category)

    private fun validRanking(ranking: List<RankedRecommendation>, ids: Set<String>): Boolean =
        (ranking.isNotEmpty() || ids.isEmpty()) && ranking.size <= ids.size &&
            ranking.map { it.eventId }.distinct().size == ranking.size && ranking.all {
                it.eventId in ids && it.reason.isNotBlank() && it.reason.length <= 180 &&
                    it.reason.none { character -> character.isISOControl() }
            }

    companion object {
        const val MAX_CANDIDATES = 24
        const val MAX_HISTORY = 20
    }
}
