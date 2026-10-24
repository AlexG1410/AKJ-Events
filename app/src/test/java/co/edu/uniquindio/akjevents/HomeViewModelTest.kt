package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
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
        assertTrue(visibleEvents().all { SamplePlaces.cityForLocation(it.location).name == "Armenia, Quindío" })
        assertTrue(visibleEvents().any { it.id == "jornada-verde" })

        viewModel.selectLocationFilter(LocationFilter.ALL)
        assertEquals(8, visibleEvents().size)
    }

    @Test
    fun greetingUsesTheSessionUser() {
        assertEquals("Camilo", viewModel.uiState.value.userFirstName)
        assertEquals("Armenia, Quindío", viewModel.uiState.value.city)
    }

    @Test
    fun featuredEventsFollowInterestVotesInsteadOfAttendance() {
        val template = repository.findPublicEvent("ciclopaseo")!!
        repository.createEvent(template.copy(id = "mas-votos", interestCount = 1000, attendanceCount = 0))
        repository.createEvent(template.copy(id = "mas-asistentes", interestCount = 0, attendanceCount = 100))

        assertEquals("mas-votos", viewModel.uiState.value.featuredEvents.first().id)
        assertFalse(viewModel.uiState.value.featuredEvents.any { it.id == "mas-asistentes" })
    }

    @Test
    fun changingVotesUpdatesFeaturedOrderAndTiesAreDeterministic() {
        val template = repository.findPublicEvent("ciclopaseo")!!
        repository.createEvent(template.copy(id = "empate-a", interestCount = 1000))
        repository.createEvent(template.copy(id = "empate-b", interestCount = 1000))
        assertEquals(listOf("empate-a", "empate-b"), viewModel.uiState.value.featuredEvents.map { it.id })

        viewModel.toggleInterest("empate-b")

        assertEquals(listOf("empate-b", "empate-a"), viewModel.uiState.value.featuredEvents.map { it.id })
    }

    @Test
    fun cityFilterUsesCoordinatesInsteadOfAddressTextAndKeepsOtherFilters() {
        val template = repository.findPublicEvent("ciclopaseo")!!
        repository.createEvent(template.copy(
            id = "sin-ciudad-en-direccion", title = "Encuentro local", address = "Carrera 13 # 20-00",
            location = SamplePlaces.cityNamed("Armenia, Quindío")!!.location
        ))
        repository.createEvent(template.copy(
            id = "direccion-enganosa", title = "Encuentro visitante", address = "Casa Armenia",
            location = SamplePlaces.cityNamed("Calarcá, Quindío")!!.location
        ))

        viewModel.selectLocationFilter(LocationFilter.CITY)
        assertTrue(visibleEvents().any { it.id == "sin-ciudad-en-direccion" })
        assertFalse(visibleEvents().any { it.id == "direccion-enganosa" })
        viewModel.onQueryChange("Encuentro")
        viewModel.selectCategory(template.category)
        assertEquals(listOf("sin-ciudad-en-direccion"), visibleEvents().map { it.id })

        val user = repos.users.currentUser.value!!
        repos.users.updateProfile(user.copy(city = "Calarcá, Quindío"))
        assertEquals(listOf("direccion-enganosa"), visibleEvents().map { it.id })
    }
}
