package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RecommendationRepositoryTest {
    @Test
    fun filtersAllIneligibleCandidatesBeforeCallingTheRanker() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("valido"), recommendationEvent("ilimitado", capacity = null),
            recommendationEvent("pendiente", status = EventStatus.PENDING),
            recommendationEvent("rechazado", status = EventStatus.REJECTED),
            recommendationEvent("finalizado", status = EventStatus.FINISHED),
            recommendationEvent("pasado", start = recommendationNow.minusSeconds(1)),
            recommendationEvent("ahora", start = recommendationNow),
            recommendationEvent("propio", owner = "lector"),
            recommendationEvent("lleno", capacity = 10, attendance = 10),
            recommendationEvent("sobrecupo", capacity = 10, attendance = 11),
            recommendationEvent("confirmado"), recommendationEvent("checkin")
        ), attendances = listOf(Attendance("confirmado", "lector"), Attendance("checkin", "lector", true)))
        var received: RecommendationRequest? = null
        val ranker = RecommendationRanker { request ->
            received = request
            request.candidates.map { RankedRecommendation(it.event.id, "Opción disponible") }
        }
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, ranker, now = { recommendationNow })

        val result = repository.recommend()

        assertEquals(setOf("valido", "ilimitado"), received!!.candidates.map { it.event.id }.toSet())
        assertEquals(setOf("valido", "ilimitado"), result.items.map { it.event.id }.toSet())
    }

    @Test
    fun contextOnlyContainsOwnLikesAndCheckInsOnVerifiedOrFinishedEvents() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("oferta"), recommendationEvent("gusto"),
            recommendationEvent("historia", status = EventStatus.FINISHED),
            recommendationEvent("pendiente", status = EventStatus.PENDING),
            recommendationEvent("rechazado", status = EventStatus.REJECTED),
            recommendationEvent("solo-confirmado"), recommendationEvent("otro-usuario")
        ), interests = setOf(EventCategory.CULTURE), liked = listOf(
            Interest("gusto", "lector"), Interest("pendiente", "lector"),
            Interest("rechazado", "lector"), Interest("otro-usuario", "otro")
        ), attendances = listOf(Attendance("historia", "lector", true), Attendance("solo-confirmado", "lector")))
        var received: RecommendationRequest? = null
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker { request ->
            received = request
            request.candidates.map { RankedRecommendation(it.event.id, "Por tus preferencias") }
        }, now = { recommendationNow })

        repository.recommend()

        val request = requireNotNull(received)
        assertEquals(setOf(EventCategory.CULTURE), request.interests)
        assertEquals(setOf("gusto", "historia"), request.interactions.map { it.event.id }.toSet())
        assertTrue(request.interactions.single { it.event.id == "historia" }.checkedIn)
    }

    @Test
    fun inventedRepeatedEmptyOrInvalidExplanationsFallBackToLocalRanking() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")))
        val local = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow }).recommend()
        val invalid = listOf(
            listOf(RankedRecommendation("inventado", "Motivo")),
            listOf(RankedRecommendation("a", "Motivo"), RankedRecommendation("a", "Motivo")),
            listOf(RankedRecommendation("a", "  ")),
            listOf(RankedRecommendation("a", "x".repeat(181))),
            emptyList()
        )
        invalid.forEach { response ->
            val repository = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker { response }, now = { recommendationNow })
            assertEquals(local, repository.recommend())
        }
    }

    @Test
    fun serviceFailureFallsBackButCancellationIsNotSwallowed() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users,
            RecommendationRanker { error("Servicio no disponible") }, now = { recommendationNow })
        assertEquals(listOf("a"), repository.recommend().items.map { it.event.id })
        val cancelled = LocalRecommendationRepository(fixture.events, fixture.users,
            RecommendationRanker { throw CancellationException("Cancelado") }, now = { recommendationNow })
        try {
            cancelled.recommend()
            fail("La cancelación debe propagarse")
        } catch (_: CancellationException) { }
    }

    @Test
    fun validResponseIsResolvedByIdInTheReturnedOrder() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker {
            listOf(RankedRecommendation("b", "Motivo breve"), RankedRecommendation("a", "Otro motivo"))
        }, now = { recommendationNow })

        val result = repository.recommend()
        assertEquals(listOf("b", "a"), result.items.map { it.event.id })
        assertEquals("Motivo breve", result.items.first().reason)
    }

    @Test
    fun availabilityAndAttendanceAreRecheckedAfterRanking() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("se-llena", capacity = 1), recommendationEvent("confirmado"), recommendationEvent("valido")))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker { request ->
            fixture.events.confirmAttendance("se-llena", "otro")
            fixture.events.confirmAttendance("confirmado", "lector")
            request.candidates.map { RankedRecommendation(it.event.id, "Motivo") }
        }, now = { recommendationNow })

        val result = repository.recommend()
        assertEquals(listOf("valido"), result.items.map { it.event.id })
    }

    @Test
    fun publicationReturnedToReviewDuringRankingCannotBeDisplayed() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("cambia"), recommendationEvent("valido")))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker { request ->
            fixture.events.updateEvent(fixture.events.findEvent("cambia")!!, "organizador")
            request.candidates.map { RankedRecommendation(it.event.id, "Motivo") }
        }, now = { recommendationNow })
        assertEquals(listOf("valido"), repository.recommend().items.map { it.event.id })
    }

    @Test
    fun expiredEventsAreRemovedBeforePublishingAndBeforeRendering() = runTest {
        var clock = recommendationNow
        val fixture = RecommendationFixture(listOf(recommendationEvent("vence", start = clock.plusSeconds(1))))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { clock })
        val feed = repository.recommend()
        assertEquals(1, feed.items.size)
        clock = clock.plusSeconds(1)
        assertTrue(repository.revalidate(feed).items.isEmpty())
        assertTrue(repository.recommend().items.isEmpty())
    }

    @Test
    fun logoutOrSessionChangeDoesNotLeakOldRecommendations() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow })
        val feed = repository.recommend()
        fixture.users.logout()
        assertTrue(repository.revalidate(feed).items.isEmpty())
        assertTrue(repository.recommend().items.isEmpty())
        val duringRequest = LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker {
            fixture.users.logout()
            listOf(RankedRecommendation("a", "Motivo"))
        }, now = { recommendationNow })
        fixture.users.login(fixture.user.email, "Comunidad1")
        assertTrue(duringRequest.recommend().items.isEmpty())
    }

    @Test
    fun requestIsBoundedAndDescriptionsAreBrief() = runTest {
        val fixture = RecommendationFixture((1..50).map {
            recommendationEvent("evento-$it", title = "t".repeat(300), description = "d".repeat(2000))
        }, liked = (1..50).map { Interest("evento-$it", "lector") })
        var received: RecommendationRequest? = null
        LocalRecommendationRepository(fixture.events, fixture.users, RecommendationRanker { request ->
            received = request
            request.candidates.map { RankedRecommendation(it.event.id, "Motivo") }
        }, now = { recommendationNow }).recommend()

        val request = requireNotNull(received)
        assertEquals(24, request.candidates.size)
        assertEquals(20, request.interactions.size)
        assertTrue(request.candidates.all { it.event.title.length <= 100 && it.event.description.length <= 500 })
    }
}
