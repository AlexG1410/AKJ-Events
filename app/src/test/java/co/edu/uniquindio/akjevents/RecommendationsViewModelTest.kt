package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRepository
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.GetRecommendationsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.ObserveRecommendationChangesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.RevalidateRecommendationsUseCase
import co.edu.uniquindio.akjevents.features.recommendations.RecommendationsViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import co.edu.uniquindio.akjevents.domain.model.RecommendationSource
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RecommendationsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sharedFeedReactsToConfirmationInterestsProfileAndLogout() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("deportes"), recommendationEvent("cultura", category = EventCategory.CULTURE)
        ))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, TestResourceProvider, now = { recommendationNow })
        val viewModel = recommendationsViewModel(repository) { recommendationNow }
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.feed.items.size)

        fixture.events.toggleInterest("cultura", "lector")
        assertEquals("cultura", viewModel.uiState.value.feed.items.first().event.id)
        fixture.events.confirmAttendance("cultura", "lector")
        assertEquals(listOf("deportes"), viewModel.uiState.value.feed.items.map { it.event.id })
        fixture.users.updateProfile(fixture.users.currentUser.value!!.copy(interests = setOf(EventCategory.SPORTS)))
        assertTrue(viewModel.uiState.value.feed.hasPersonalization)
        viewModel.refresh()
        assertEquals(listOf("deportes"), viewModel.uiState.value.feed.items.map { it.event.id })
        fixture.users.logout()
        assertTrue(viewModel.uiState.value.feed.items.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startingEventExpiresWithoutAnyRepositoryMutation() = runTest {
        var clock = recommendationNow
        val fixture = RecommendationFixture(listOf(recommendationEvent("vence", start = clock.plusSeconds(1))))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, TestResourceProvider, now = { clock })
        val viewModel = recommendationsViewModel(repository) { clock }
        assertEquals(1, viewModel.uiState.value.feed.items.size)

        clock = clock.plusSeconds(1)
        advanceTimeBy(1000)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.feed.items.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test fun relevantProjectionDebounceAndCacheAvoidDuplicateOrIrrelevantCalls() = runTest {
        val fixture = RecommendationFixture(listOf(recommendationEvent("a"), recommendationEvent("b")))
        var calls = 0
        val ranker = object : RecommendationRanker {
            override val usesRemoteService = true
            override suspend fun rank(request: RecommendationRequest): List<RankedRecommendation> {
                calls++
                return request.candidates.map { RankedRecommendation(it.event.id, "Motivo IA") }
            }
        }
        val repo = LocalRecommendationRepository(fixture.events, fixture.users, TestResourceProvider, ranker, now = { recommendationNow })
        var emissions = 0
        repo.changes.onEach { emissions++ }.launchIn(backgroundScope)
        runCurrent()
        val vm = RecommendationsViewModel(ObserveRecommendationChangesUseCase(repo), GetRecommendationsUseCase(repo), RevalidateRecommendationsUseCase(repo), { recommendationNow }, 750)
        assertEquals(0, calls)
        advanceTimeBy(750); runCurrent()
        assertEquals(1, calls)
        assertEquals(RecommendationSource.REMOTE, vm.uiState.value.feed.source)
        val baseline = emissions
        fixture.events.toggleInterest("a", "someone-else")
        fixture.users.updateProfile(fixture.users.currentUser.value!!.copy(name = "Nuevo nombre"))
        advanceTimeBy(1000); runCurrent()
        assertEquals(baseline, emissions)
        assertEquals(1, calls)
        // Dos consumidores pueden pedir refresco: la última petición agrupa el trabajo y la caché evita red.
        vm.refresh(); vm.refresh()
        advanceTimeBy(750); runCurrent()
        assertEquals(1, calls)
        fixture.events.toggleInterest("b", "lector")
        advanceTimeBy(750); runCurrent()
        assertEquals(1, calls)
        assertEquals(RecommendationSource.LOCAL, vm.uiState.value.feed.source)
        fixture.users.logout()
        assertTrue(vm.visibleFeed(vm.uiState.value.feed).items.isEmpty())
        advanceTimeBy(750); runCurrent()
        assertTrue(vm.uiState.value.feed.items.isEmpty())
    }
}

private fun recommendationsViewModel(repository: RecommendationRepository, now: () -> LocalDateTime) = RecommendationsViewModel(
    ObserveRecommendationChangesUseCase(repository),
    GetRecommendationsUseCase(repository),
    RevalidateRecommendationsUseCase(repository),
    now
)
