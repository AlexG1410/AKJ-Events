package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.features.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryEventRepository()
    // Perezoso: se crea dentro de la prueba, cuando MainDispatcherRule ya instaló el dispatcher
    private val viewModel by lazy { HomeViewModel(repository) }

    private fun visibleEvents() = viewModel.uiState.value.let { it.featuredEvents + it.upcomingEvents }

    @Test
    fun feedOnlyShowsVerifiedEvents() {
        val events = visibleEvents()

        assertEquals(5, events.size)
        assertTrue(events.all { it.status == EventStatus.VERIFIED })
        assertEquals(HomeViewModel.FEATURED_COUNT, viewModel.uiState.value.featuredEvents.size)
    }

    @Test
    fun searchFiltersByTitleIgnoringCaseAndAccents() {
        viewModel.onQueryChange("CICLOPASEO")
        assertEquals(listOf("ciclopaseo"), visibleEvents().map { it.id })

        viewModel.onQueryChange("jornada de limpieza parque de la vída")
        assertEquals(listOf("jornada-verde"), visibleEvents().map { it.id })
    }

    @Test
    fun searchDoesNotRevealHiddenEvents() {
        viewModel.onQueryChange("Evento en revisión")

        assertFalse(viewModel.uiState.value.hasResults)
    }

    @Test
    fun categoryAndSearchAreCombined() {
        viewModel.selectCategory(EventCategory.ACADEMIC)
        assertEquals(listOf("taller-kotlin"), visibleEvents().map { it.id })

        viewModel.onQueryChange("ciclopaseo")
        assertFalse(viewModel.uiState.value.hasResults)

        viewModel.selectCategory(null)
        assertEquals(listOf("ciclopaseo"), visibleEvents().map { it.id })
    }

    @Test
    fun toggleInterestGoesThroughTheRepository() {
        val before = repository.findPublicEvent("feria-cultural")!!.interestCount

        viewModel.toggleInterest("feria-cultural")

        assertTrue("feria-cultural" in viewModel.uiState.value.interestedEventIds)
        assertEquals(before + 1, visibleEvents().first { it.id == "feria-cultural" }.interestCount)

        viewModel.toggleInterest("feria-cultural")
        assertFalse("feria-cultural" in viewModel.uiState.value.interestedEventIds)
    }

    @Test
    fun filtersStayAppliedWhenTheRepositoryChanges() {
        viewModel.onQueryChange("kotlin")

        repository.confirmAttendance("ciclopaseo")

        assertEquals(listOf("taller-kotlin"), visibleEvents().map { it.id })
    }
}
