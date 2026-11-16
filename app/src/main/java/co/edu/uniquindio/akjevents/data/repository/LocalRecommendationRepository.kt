package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationCandidate
import co.edu.uniquindio.akjevents.domain.model.RecommendationEvent
import co.edu.uniquindio.akjevents.domain.model.RecommendationFeed
import co.edu.uniquindio.akjevents.domain.model.RecommendationInteraction
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import co.edu.uniquindio.akjevents.domain.model.RecommendedEvent
import co.edu.uniquindio.akjevents.domain.model.RecommendationSource
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Filtra antes de clasificar y valida de nuevo usando los datos actuales al recibir la respuesta. */
@Singleton
class LocalRecommendationRepository(
    private val events: EventRepository,
    private val users: UserRepository,
    private val resources: ResourceProvider,
    private val ranker: RecommendationRanker = LocalRecommendationRanker(resources),
    private val now: () -> LocalDateTime = LocalDateTime::now
) : RecommendationRepository {

    @Inject
    constructor(events: EventRepository, users: UserRepository, resources: ResourceProvider, ranker: RecommendationRanker) :
        this(events, users, resources, ranker, LocalDateTime::now)

    // Si la clasificación principal falla o responde algo inválido, se usa la local
    private val fallback = LocalRecommendationRanker(resources)
    private data class Cache(val userId: String, val request: RecommendationRequest, val ranking: List<RankedRecommendation>, val source: RecommendationSource, val until: LocalDateTime)
    private var cache: Cache? = null
    private var lastAttemptUser: String? = null
    private var lastAttemptAt: LocalDateTime? = null

    override val changes: Flow<Unit> = combine(
        events.publicEvents, events.events, events.interests, events.attendances, users.currentUser
    ) { _, _, _, _, _ ->
        users.currentUser.value?.takeIf { it.role == UserRole.USER }?.let { it.id to context(it) }
    }.distinctUntilChanged().map { }

    override suspend fun recommend(): RecommendationFeed {
        val user = users.currentUser.value?.takeIf { it.role == UserRole.USER } ?: run {
            cache = null
            lastAttemptUser = null
            lastAttemptAt = null
            return RecommendationFeed()
        }
        val initialContext = context(user)
        var request = shortlist(initialContext)
        var source = RecommendationSource.LOCAL
        var local = fallback.rank(request)
        val cached = cache?.takeIf { it.userId == user.id && it.request == request && now().isBefore(it.until) }
        val coolingDown = lastAttemptUser == user.id && lastAttemptAt?.plusSeconds(60)?.isAfter(now()) == true
        var response = when {
            request.candidates.isEmpty() -> local
            ranker.usesRemoteService && cached != null -> {
                source = cached.source
                cached.ranking
            }
            ranker.usesRemoteService && coolingDown -> local
            else -> {
                if (ranker.usesRemoteService) {
                    lastAttemptUser = user.id
                    lastAttemptAt = now()
                }
                val ranked = try {
                    ranker.rank(request).also { currentCoroutineContext().ensureActive() }
                        .takeIf { validRanking(it, request.candidates.map { c -> c.event.id }.toSet()) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
                if (ranked != null && ranker.usesRemoteService) source = RecommendationSource.REMOTE
                (ranked ?: local).also {
                    if (ranker.usesRemoteService) cache = Cache(user.id, request, it, source, now().plusSeconds(if (source == RecommendationSource.REMOTE) 300 else 60))
                }
            }
        }
        val currentUser = users.currentUser.value
        if (currentUser?.id != user.id || currentUser.role != UserRole.USER) return RecommendationFeed()
        // Ni una respuesta remota válida puede mantener un contexto que cambió mientras esperaba.
        val currentContext = context(currentUser)
        if (currentContext != initialContext) {
            request = shortlist(currentContext)
            local = fallback.rank(request)
            response = local
            source = RecommendationSource.LOCAL
        }
        val byId = eligibleEvents(currentUser).associateBy { it.id }
        val feed = RecommendationFeed(
            userId = user.id,
            items = response.mapNotNull { rankedEvent ->
                byId[rankedEvent.eventId]?.let { event ->
                    RecommendedEvent(event, rankedEvent.reason.trim(), currentUser.location.distanceKmTo(event.location))
                }
            },
            hasPersonalization = request.interests.isNotEmpty() || request.interactions.isNotEmpty(),
            source = source
        )
        return revalidate(feed)
    }

    /** Proyección semántica: no incluye puntos, foto, comentarios ni votos de otras personas. */
    private fun context(user: User): RecommendationRequest {
        val candidates = eligibleEvents(user).sortedBy { it.id }.map { candidate(it, user) }
        val liked = events.interests.value.filter { it.userId == user.id }.map { it.eventId }.toSet()
        val checkedIn = events.attendances.value.filter { it.userId == user.id && it.checkedIn }.map { it.eventId }.toSet()
        val history = events.events.value.filter {
            (it.id in liked || it.id in checkedIn) &&
                (it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED)
        }.sortedWith(compareByDescending<CommunityEvent> { it.startsAt }.thenBy { it.id })
            .take(MAX_HISTORY).map { RecommendationInteraction(it.brief(), it.id in checkedIn) }
        return RecommendationRequest(user.interests, history, candidates, resources.getString(R.string.ai_reason_language))
    }

    private suspend fun shortlist(fullRequest: RecommendationRequest): RecommendationRequest {
        val shortlist = fallback.rank(fullRequest).take(MAX_CANDIDATES).map { it.eventId }.toSet()
        return fullRequest.copy(candidates = fullRequest.candidates.filter { it.event.id in shortlist })
    }

    override fun revalidate(feed: RecommendationFeed): RecommendationFeed {
        val user = users.currentUser.value
        if (user == null || user.role != UserRole.USER || feed.userId != user.id) return RecommendationFeed()
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
