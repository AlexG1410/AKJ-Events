package co.edu.uniquindio.akjevents.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

data class HomeUiState(
    val userFirstName: String = "",
    val city: String = "",
    val query: String = "",
    val category: EventCategory? = null,
    val featuredEvents: List<CommunityEvent> = emptyList(),
    val upcomingEvents: List<CommunityEvent> = emptyList(),
    val interestedEventIds: Set<String> = emptySet()
) {
    val hasResults: Boolean get() = featuredEvents.isNotEmpty() || upcomingEvents.isNotEmpty()
}

class HomeViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared
) : ViewModel() {
    // El repositorio solo entrega eventos verificados (públicos)
    private var publicEvents: List<CommunityEvent> = repository.publicEvents.value

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userFirstName = SampleUsers.currentUser.name.substringBefore(" "),
            city = SampleUsers.currentUser.city,
            interestedEventIds = repository.interestedEventIds.value
        ).filtered(publicEvents)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Refleja los cambios hechos desde otras pantallas (asistencia, "Me interesa")
        viewModelScope.launch {
            combine(repository.publicEvents, repository.interestedEventIds, ::Pair).collect { (events, interested) ->
                publicEvents = events
                _uiState.update { it.copy(interestedEventIds = interested).filtered(events) }
            }
        }
    }

    // Los filtros se aplican al momento para que el campo de búsqueda responda sin retraso
    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered(publicEvents) }

    fun selectCategory(category: EventCategory?) = _uiState.update { it.copy(category = category).filtered(publicEvents) }

    fun toggleInterest(eventId: String) {
        repository.toggleInterest(eventId)
    }

    companion object {
        const val FEATURED_COUNT = 2

        /** Destacados: los de más asistentes confirmados. El resto se lista por fecha. */
        private fun HomeUiState.filtered(events: List<CommunityEvent>): HomeUiState {
            val search = query.normalizedForSearch()
            val matching = events.filter { event ->
                (category == null || event.category == category) &&
                    (search.isEmpty() || event.title.normalizedForSearch().contains(search))
            }
            val featured = matching.sortedByDescending { it.attendanceCount }.take(FEATURED_COUNT)
            val featuredIds = featured.map { it.id }.toSet()
            return copy(
                featuredEvents = featured,
                upcomingEvents = matching.filter { it.id !in featuredIds }.sortedBy { it.startsAt }
            )
        }

        /** Minúsculas y sin tildes, para que "academico" encuentre "Académico". */
        private fun String.normalizedForSearch(): String =
            Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
    }
}
