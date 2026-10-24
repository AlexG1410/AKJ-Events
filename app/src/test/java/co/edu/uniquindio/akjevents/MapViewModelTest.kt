package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.features.map.explore.MapViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class MapViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = LocalDateTime.of(2026, 10, 23, 12, 0)
    private val repos = TestRepositories()

    private fun event(id: String, start: LocalDateTime) = SampleEvents.all.first().copy(
        id = id, startsAt = start, endsAt = start.plusHours(2), status = EventStatus.VERIFIED,
        category = EventCategory.SPORTS, location = SamplePlaces.cityNamed("Armenia, Quindío")!!.location
    )

    @Test
    fun thisWeekIncludesNowButExcludesPastAndSevenDayBoundary() {
        val events = listOf(
            event("pasado", now.minusSeconds(1)), event("ahora", now),
            event("dentro", now.plusDays(7).minusSeconds(1)),
            event("limite", now.plusDays(7)), event("despues", now.plusDays(8))
        )
        val repository = InMemoryEventRepository(initialEvents = events, users = repos.users)
        val viewModel = MapViewModel(repository, repos.users, now = { now })

        viewModel.toggleThisWeek()
        assertEquals(listOf("ahora", "dentro"), viewModel.uiState.value.events.map { it.id })
        viewModel.toggleThisWeek()
        assertEquals(events.map { it.id }, viewModel.uiState.value.events.map { it.id })
    }

    @Test
    fun dateFilterCombinesWithSearchCategoryDistanceAndVisibility() {
        val base = event("visible", now.plusDays(1)).copy(title = "Encuentro deportivo")
        val repository = InMemoryEventRepository(initialEvents = listOf(
            base, base.copy(id = "pasado", startsAt = now.minusDays(1)),
            base.copy(id = "pendiente", status = EventStatus.PENDING),
            base.copy(id = "otra-categoria", category = EventCategory.CULTURE),
            base.copy(id = "otro-titulo", title = "Actividad distinta"),
            base.copy(id = "lejos", location = SamplePlaces.cityNamed("Pereira, Risaralda")!!.location)
        ), users = repos.users)
        val viewModel = MapViewModel(repository, repos.users, now = { now })

        viewModel.toggleThisWeek()
        viewModel.toggleNearby()
        viewModel.selectCategory(EventCategory.SPORTS)
        viewModel.onQueryChange("encuentro")

        assertEquals(listOf("visible"), viewModel.uiState.value.events.map { it.id })
    }
}
