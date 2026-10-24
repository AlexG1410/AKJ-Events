package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.features.recommendations.RecommendationsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RecommendationsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun sharedFeedReactsToConfirmationInterestsProfileAndLogout() {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("deportes"), recommendationEvent("cultura", category = EventCategory.CULTURE)
        ))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow })
        val viewModel = RecommendationsViewModel(repository, now = { recommendationNow })
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
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { clock })
        val viewModel = RecommendationsViewModel(repository, now = { clock })
        assertEquals(1, viewModel.uiState.value.feed.items.size)

        clock = clock.plusSeconds(1)
        advanceTimeBy(1000)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.feed.items.isEmpty())
    }
}
