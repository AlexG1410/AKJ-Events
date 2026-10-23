package co.edu.uniquindio.akjevents.features.map.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class MapUiState(
    val user: User? = null,
    val query: String = "",
    val nearbyOnly: Boolean = false,
    val thisWeekOnly: Boolean = false,
    val category: EventCategory? = null,
    val events: List<CommunityEvent> = emptyList(),
    val selectedEventId: String? = null,
    val distancesKm: Map<String, Double> = emptyMap()
) {
    val selectedEvent: CommunityEvent? get() = events.find { it.id == selectedEventId }
}

/** Mockup 10: eventos públicos sobre el mapa, con filtros por cercanía, fecha y categoría. */
class MapViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : ViewModel() {
    private var publicEvents = repository.publicEvents.value
    private val _uiState = MutableStateFlow(MapUiState(user = users.currentUser.value).filtered())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.publicEvents, users.currentUser, ::Pair).collect { (events, user) ->
                publicEvents = events
                _uiState.update { it.copy(user = user).filtered() }
            }
        }
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered() }

    fun toggleNearby() = _uiState.update { it.copy(nearbyOnly = !it.nearbyOnly).filtered() }

    fun toggleThisWeek() = _uiState.update { it.copy(thisWeekOnly = !it.thisWeekOnly).filtered() }

    fun selectCategory(category: EventCategory?) = _uiState.update { it.copy(category = category).filtered() }

    fun selectEvent(eventId: String?) = _uiState.update { it.copy(selectedEventId = eventId) }

    private fun MapUiState.filtered(): MapUiState {
        val search = query.normalizedForSearch()
        val distances = user?.let { u -> publicEvents.associate { it.id to u.location.distanceKmTo(it.location) } }.orEmpty()
        val weekLimit = now().plusDays(7)
        val visible = publicEvents.filter { event ->
            (search.isEmpty() || event.title.normalizedForSearch().contains(search) || event.address.normalizedForSearch().contains(search)) &&
                (!nearbyOnly || (distances[event.id] ?: Double.MAX_VALUE) <= NEARBY_RADIUS_KM) &&
                (!thisWeekOnly || event.startsAt.isBefore(weekLimit)) &&
                (category == null || event.category == category)
        }.sortedBy { it.startsAt }
        // Si el evento elegido deja de verse, se elige el más próximo
        val selected = selectedEventId?.takeIf { id -> visible.any { it.id == id } } ?: visible.firstOrNull()?.id
        return copy(events = visible, distancesKm = distances, selectedEventId = selected)
    }

    companion object {
        const val NEARBY_RADIUS_KM = 5
    }
}
