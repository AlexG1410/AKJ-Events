package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionException
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionFailure
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionRepository
import co.edu.uniquindio.akjevents.domain.usecase.event.SuggestEventCategoryUseCase
import co.edu.uniquindio.akjevents.features.event.create.CategorySuggestionStatus
import co.edu.uniquindio.akjevents.features.event.create.CreateEventViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategorySuggestionViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repos = TestRepositories()
    private val calls = mutableListOf<Pair<String, String>>()
    private fun vm(eventId: String? = null, answer: CategorySuggestionRepository = CategorySuggestionRepository { title, description ->
        calls += title to description
        EventCategory.ACADEMIC
    }) = repos.useCases.let {
        CreateEventViewModel(it.getEventById, it.observeCurrentUser, it.createEvent, it.updateEvent, it.uploadImages,
            TestResourceProvider, SuggestEventCategoryUseCase(answer), eventId, now = { LocalDateTime.of(2026, 10, 5, 12, 0) })
    }
    private fun fill(vm: CreateEventViewModel) {
        vm.onTitleChange("Taller de huertos")
        vm.onDescriptionChange("Aprenderemos juntos sobre huertos urbanos.")
    }

    @Test fun waitsForEnoughTextAndDebouncesBeforeSuggestingWithoutSelecting() = runTest {
        val vm = vm()
        vm.onTitleChange("Hola")
        vm.onDescriptionChange("Corta")
        advanceTimeBy(2000)
        assertTrue(calls.isEmpty())
        fill(vm)
        assertEquals(CategorySuggestionStatus.WAITING, vm.uiState.value.suggestionStatus)
        advanceTimeBy(1499)
        assertTrue(calls.isEmpty())
        vm.onTitleChange("Taller de ciencia")
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(1, calls.size)
        assertEquals("Taller de ciencia", calls.single().first)
        assertEquals(CategorySuggestionStatus.READY, vm.uiState.value.suggestionStatus)
        assertNull(vm.uiState.value.category)
        vm.acceptCategorySuggestion()
        assertEquals(EventCategory.ACADEMIC, vm.uiState.value.category)
        assertNull(vm.uiState.value.suggestedCategory)
    }

    @Test fun manualChoiceCancelsPendingSuggestionAndStopsAutomaticRequests() = runTest {
        val result = CompletableDeferred<EventCategory>()
        val vm = vm(answer = CategorySuggestionRepository { _, _ -> withContext(NonCancellable) { result.await() } })
        fill(vm)
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(CategorySuggestionStatus.LOADING, vm.uiState.value.suggestionStatus)
        vm.selectCategory(EventCategory.SPORTS)
        result.complete(EventCategory.CULTURE)
        runCurrent()
        assertEquals(EventCategory.SPORTS, vm.uiState.value.category)
        assertEquals(CategorySuggestionStatus.IDLE, vm.uiState.value.suggestionStatus)
        assertNull(vm.uiState.value.suggestedCategory)
        vm.onDescriptionChange("Una descripción completamente diferente del evento.")
        advanceTimeBy(2000)
        assertEquals(CategorySuggestionStatus.IDLE, vm.uiState.value.suggestionStatus)
    }

    @Test fun lateResponseCannotReplaceNewerTextOrSuggestion() = runTest {
        val old = CompletableDeferred<EventCategory>()
        val vm = vm(answer = CategorySuggestionRepository { title, _ ->
            if (title == "Taller de huertos") withContext(NonCancellable) { old.await() } else EventCategory.SPORTS
        })
        fill(vm)
        advanceTimeBy(1500)
        runCurrent()
        vm.onTitleChange("Torneo de fútbol")
        advanceTimeBy(1500)
        runCurrent()
        assertEquals(EventCategory.SPORTS, vm.uiState.value.suggestedCategory)
        old.complete(EventCategory.CULTURE)
        runCurrent()
        assertEquals(EventCategory.SPORTS, vm.uiState.value.suggestedCategory)
        assertNull(vm.uiState.value.category)
        vm.onDescriptionChange("")
        assertNull(vm.uiState.value.suggestedCategory)
        assertEquals(CategorySuggestionStatus.IDLE, vm.uiState.value.suggestionStatus)
    }

    @Test fun editKeepsStoredCategoryAndOnlySuggestsOnExplicitRequest() = runTest {
        val vm = vm(eventId = "voleibol-callejero")
        val category = vm.uiState.value.category
        vm.onDescriptionChange("Nueva descripción completa para el evento.")
        advanceTimeBy(2000)
        assertTrue(calls.isEmpty())
        assertEquals(category, vm.uiState.value.category)
        vm.requestCategorySuggestion()
        assertEquals(EventCategory.ACADEMIC, vm.uiState.value.suggestedCategory)
        assertEquals(category, vm.uiState.value.category)
    }

    @Test fun errorsAreIndependentOfSubmissionAndManualChoiceAlwaysWorks() = runTest {
        for (failure in CategorySuggestionFailure.entries) {
            val vm = vm(answer = CategorySuggestionRepository { _, _ -> throw CategorySuggestionException(failure) })
            fill(vm)
            vm.requestCategorySuggestion()
            assertEquals(CategorySuggestionStatus.ERROR, vm.uiState.value.suggestionStatus)
            assertFalse(vm.uiState.value.suggestionError.isNullOrBlank())
            assertFalse(vm.uiState.value.isBusy)
            assertNull(vm.uiState.value.result)
            vm.selectCategory(EventCategory.SOCIAL)
            assertEquals(EventCategory.SOCIAL, vm.uiState.value.category)
            vm.submit()
            // La validación continúa normalmente: falta una foto, no una respuesta de IA.
            assertEquals(RequestResult.Failure(TestResourceProvider.getString(R.string.create_error_image)), vm.uiState.value.result)
        }
    }

    @Test fun retryWorksAndIdenticalSuccessfulInputUsesFormCache() = runTest {
        var attempts = 0
        val vm = vm(answer = CategorySuggestionRepository { _, _ ->
            if (++attempts == 1) throw IllegalStateException("test network failure")
            EventCategory.CULTURE
        })
        fill(vm)
        vm.requestCategorySuggestion()
        assertEquals(CategorySuggestionStatus.ERROR, vm.uiState.value.suggestionStatus)
        vm.requestCategorySuggestion()
        assertEquals(EventCategory.CULTURE, vm.uiState.value.suggestedCategory)
        vm.requestCategorySuggestion()
        assertEquals(2, attempts)
    }

    @Test fun failedAiDoesNotPreventPublishingWithAManuallyChosenCategory() = runTest {
        val vm = vm(answer = CategorySuggestionRepository { _, _ -> throw CategorySuggestionException(CategorySuggestionFailure.UNAVAILABLE) })
        fill(vm)
        vm.requestCategorySuggestion()
        assertEquals(CategorySuggestionStatus.ERROR, vm.uiState.value.suggestionStatus)
        vm.selectCategory(EventCategory.VOLUNTEERING)
        vm.addImage("https://images.example.test/huertos.jpg")
        vm.onLocationPicked(PickedLocation("Parque de prueba", Location(4.55, -75.66)))
        vm.onStartsAtChange(LocalDateTime.of(2026, 10, 7, 9, 0))
        vm.onEndsAtChange(LocalDateTime.of(2026, 10, 7, 12, 0))
        vm.submit()
        assertTrue(vm.uiState.value.result is RequestResult.Success)
        val created = repos.events.events.value.single { it.title == "Taller de huertos" }
        assertEquals(EventCategory.VOLUNTEERING, created.category)
        assertEquals(EventStatus.PENDING, created.status)
    }

    @Test fun repeatedClickDoesNotStartParallelCallsAndLogoutDiscardsResponse() = runTest {
        val pending = CompletableDeferred<EventCategory>()
        var attempts = 0
        val vm = vm(answer = CategorySuggestionRepository { _, _ -> attempts++; pending.await() })
        fill(vm)
        vm.requestCategorySuggestion()
        vm.requestCategorySuggestion()
        assertEquals(1, attempts)
        repos.users.logout()
        pending.complete(EventCategory.SPORTS)
        runCurrent()
        assertNull(vm.uiState.value.suggestedCategory)
        assertEquals(CategorySuggestionStatus.IDLE, vm.uiState.value.suggestionStatus)
    }
}
