package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.remote.ai.*
import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.data.repository.remote.RemoteRecommendationRanker
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import okhttp3.ResponseBody.Companion.toResponseBody
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.runBlocking

class RemoteRecommendationRankerTest {
    private fun adapter(answer: suspend (RecommendationRequestDto) -> RecommendationResponseDto) = RemoteRecommendationRanker(
        object : RecommendationApi {
            override suspend fun recommend(authorization: String, request: RecommendationRequestDto): RecommendationResponseDto {
                assertEquals("Bearer fake-token", authorization)
                return answer(request)
            }
        }, { "fake-token" }, AiServiceConfig("https://worker.test/")
    )
    private fun repository(fixture: RecommendationFixture, ranker: RecommendationRanker, clock: () -> java.time.LocalDateTime = { recommendationNow }) =
        LocalRecommendationRepository(fixture.events, fixture.users, TestResourceProvider, ranker, clock)
    private fun valid(request: RecommendationRequestDto) = RecommendationResponseDto(request.candidates.reversed().map { RankedRecommendationDto(it.eventId, "Motivo remoto") })

    @Test fun remoteOrderAndSourceAreVisibleAndOnlyMinimumContextIsSent() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")), interests = setOf(EventCategory.SPORTS), liked = listOf(Interest("a", "lector")))
        var payload: RecommendationRequestDto? = null
        val result = repository(fixture, adapter { payload = it; valid(it) }).recommend()
        assertEquals(listOf("b", "a"), result.items.map { it.event.id })
        assertEquals(RecommendationSource.REMOTE, result.source)
        val sent = requireNotNull(payload)
        assertEquals(listOf("SPORTS"), sent.interests)
        assertEquals("es", sent.language)
        val json = com.google.gson.Gson().toJson(sent)
        for (privateValue in listOf("ana@", "Dirección privada", "latitude", "longitude", "distanceKm", "remainingSpots", "lector")) assertFalse(json.contains(privateValue))
        assertTrue(sent.candidates.all { it.description.length <= 200 })
    }

    @Test fun invalidRemoteResponsesFallBackLocallyWithAnHonestSource() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")))
        val local = LocalRecommendationRepository(fixture.events, fixture.users, TestResourceProvider, now = { recommendationNow }).recommend()
        for (items in listOf(
            null, emptyList(), listOf(null), listOf(RankedRecommendationDto("a", "ok")),
            listOf(RankedRecommendationDto("invented", "ok"), RankedRecommendationDto("b", "ok")),
            listOf(RankedRecommendationDto("a", "ok"), RankedRecommendationDto("a", "ok")),
            listOf(RankedRecommendationDto("a", " "), RankedRecommendationDto("b", "ok")),
            listOf(RankedRecommendationDto("a", "x".repeat(181)), RankedRecommendationDto("b", "ok")),
            listOf(RankedRecommendationDto("a", "bad\nline"), RankedRecommendationDto("b", "ok"))
        )) {
            val result = repository(fixture, adapter { RecommendationResponseDto(items) }).recommend()
            assertEquals(local, result)
            assertEquals(RecommendationSource.LOCAL, result.source)
        }
    }

    @Test fun networkQuotaAndTimeoutFailuresFallBackButCancellationPropagates() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
        for (ranker in listOf(adapter { throw java.io.IOException("network") }, adapter { delay(23_000); valid(it) },
            adapter { throw retrofit2.HttpException(retrofit2.Response.error<Unit>(429, "quota".toResponseBody())) })) {
            assertEquals(RecommendationSource.LOCAL, repository(fixture, ranker).recommend().source)
        }
        try { repository(fixture, adapter { throw CancellationException() }).recommend(); fail("Cancellation must propagate") }
        catch (_: CancellationException) { }
    }

    @Test fun missingEndpointAndNoCandidatesNeverReadTokensOrMakeNetworkCalls() = runTest {
        val api = object : RecommendationApi {
            override suspend fun recommend(authorization: String, request: RecommendationRequestDto): RecommendationResponseDto = error("must not call")
        }
        val ranker = RemoteRecommendationRanker(api, { error("must not read token") }, AiServiceConfig(""))
        val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
        assertEquals(RecommendationSource.LOCAL, repository(fixture, ranker).recommend().source)
        val empty = RecommendationFixture(emptyList())
        assertTrue(repository(empty, ranker).recommend().items.isEmpty())
    }

    @Test fun sameContextIsCachedAndFailuresHaveCooldownEvenWhenContextChanges() = runTest {
        var clock = recommendationNow
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")))
        var calls = 0
        val repo = repository(fixture, adapter { calls++; valid(it) }) { clock }
        assertEquals(RecommendationSource.REMOTE, repo.recommend().source)
        repeat(3) { assertEquals(RecommendationSource.REMOTE, repo.recommend().source) }
        assertEquals(1, calls)
        fixture.events.toggleInterest("a", "lector")
        assertEquals(RecommendationSource.LOCAL, repo.recommend().source)
        assertEquals(1, calls)
        clock = clock.plusSeconds(61)
        assertEquals(RecommendationSource.REMOTE, repo.recommend().source)
        assertEquals(2, calls)
        clock = clock.plusMinutes(6)
        repo.recommend()
        assertEquals(3, calls)
        var failedCalls = 0
        val failed = repository(fixture, adapter { failedCalls++; error("unavailable") }) { clock }
        repeat(3) { assertEquals(RecommendationSource.LOCAL, failed.recommend().source) }
        assertEquals(1, failedCalls)
    }

    @Test fun sessionChangeOrLostEligibilityWhileWaitingCannotPublishStaleRemoteItems() = runTest {
        for (logout in listOf(false, true)) {
            val fixture = RecommendationFixture(listOf(recommendationEvent("a", capacity = 1), recommendationEvent("b")))
            val started = CompletableDeferred<Unit>()
            val release = CompletableDeferred<Unit>()
            val repo = repository(fixture, adapter { started.complete(Unit); release.await(); valid(it) })
            val waiting = async { repo.recommend() }
            started.await()
            if (logout) fixture.users.logout() else fixture.events.confirmAttendance("a", "someone")
            release.complete(Unit)
            val result = waiting.await()
            if (logout) assertTrue(result.items.isEmpty()) else {
                assertEquals(listOf("b"), result.items.map { it.event.id })
                assertEquals(RecommendationSource.LOCAL, result.source)
            }
        }
    }

    @Test fun profileContextChangesDuringRequestDiscardRemoteReasonsAndLocalResultUsesNewInterests() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b", category = EventCategory.CULTURE)))
        val repo = repository(fixture, adapter {
            fixture.users.updateProfile(fixture.users.currentUser.value!!.copy(interests = setOf(EventCategory.CULTURE)))
            valid(it)
        })
        val result = repo.recommend()
        assertEquals(RecommendationSource.LOCAL, result.source)
        assertEquals("b", result.items.first().event.id)
    }

    @Test fun switchingToAnotherUserDuringResponseNeverPublishesPreviousUsersRanking() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val repo = repository(fixture, adapter { started.complete(Unit); release.await(); valid(it) })
        val result = async { repo.recommend() }
        started.await()
        fixture.users.logout()
        fixture.users.register("Otra persona", "other@example.test", "Ciudad", "Comunidad1", emptySet(), "Lugar de prueba", recommendationLocation)
        assertNotEquals("lector", fixture.users.currentUser.value?.id)
        release.complete(Unit)
        assertTrue(result.await().items.isEmpty())
    }

    @Test fun retrofitPostsToIndependentRouteAndValidatesActualJsonContract() = runBlocking {
        val recorded = AtomicReference<String>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/recommend-events") { exchange ->
            assertEquals("POST", exchange.requestMethod)
            assertEquals("Bearer fake-token", exchange.requestHeaders.getFirst("Authorization"))
            recorded.set(exchange.requestBody.bufferedReader().readText())
            val body = "{\"items\":[{\"eventId\":\"a\",\"reason\":\"Motivo remoto\"}]}".toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
            exchange.close()
        }
        server.start()
        try {
            val api = retrofit2.Retrofit.Builder().baseUrl("http://127.0.0.1:${server.address.port}/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create()).build().create(RecommendationApi::class.java)
            val fixture = RecommendationFixture(listOf(recommendationEvent("a")))
            val ranker = RemoteRecommendationRanker(api, { "fake-token" }, AiServiceConfig("https://worker.test/"))
            assertEquals(RecommendationSource.REMOTE, repository(fixture, ranker).recommend().source)
            val json = com.google.gson.JsonParser.parseString(recorded.get()).asJsonObject
            assertEquals(setOf("interests", "interactions", "candidates", "language"), json.keySet())
            assertEquals(setOf("eventId", "title", "description", "category"), json.getAsJsonArray("candidates")[0].asJsonObject.keySet())
        } finally { server.stop(0) }
    }
}
