package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.features.home.HomeViewModel
import co.edu.uniquindio.akjevents.features.home.LocationFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories()
    private val repository = repos.events
    // Perezoso: se crea dentro de la prueba, cuando MainDispatcherRule ya instaló el dispatcher
    private val viewModel by lazy { HomeViewModel(repository, repos.users) }

    private fun visibleEvents() = viewModel.uiState.value.let { it.featuredEvents + it.upcomingEvents }

    @Test
    fun feedOnlyShowsVerifiedEvents() {
        val events = visibleEvents()

        assertEquals(8, events.size)
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
        assertEquals(setOf("taller-kotlin", "hackaton-utp"), visibleEvents().map { it.id }.toSet())

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

        repository.confirmAttendance("ciclopaseo", TestRepositories.DEMO_USER)

        assertEquals(listOf("taller-kotlin"), visibleEvents().map { it.id })
    }

    @Test
    fun locationFilterKeepsOnlyNearbyOrSameCityEvents() {
        viewModel.selectLocationFilter(LocationFilter.NEARBY)
        val nearby = visibleEvents().map { it.id }.toSet()
        assertFalse("caminata-cocora" in nearby)
        assertFalse("hackaton-utp" in nearby)
        assertTrue("ciclopaseo" in nearby)

        viewModel.selectLocationFilter(LocationFilter.CITY)
        assertEquals(6, visibleEvents().size)
        assertTrue(visibleEvents().all { it.address.contains("Armenia") })

        viewModel.selectLocationFilter(LocationFilter.ALL)
        assertEquals(8, visibleEvents().size)
    }

    @Test
    fun greetingUsesTheSessionUser() {
        assertEquals("Camilo", viewModel.uiState.value.userFirstName)
        assertEquals("Armenia, Quindío", viewModel.uiState.value.city)
    }
}
