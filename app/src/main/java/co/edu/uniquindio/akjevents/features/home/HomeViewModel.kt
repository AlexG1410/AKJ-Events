package co.edu.uniquindio.akjevents.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObservePublicEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveInterestsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ToggleInterestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtro del feed por ubicación respecto al usuario. */
enum class LocationFilter(val label: String) {
    ALL("Todas"),
    NEARBY("Cerca de mí (${HomeViewModel.NEARBY_RADIUS_KM} km)"),
    CITY("En mi ciudad")
}

data class HomeUiState(
    val user: User? = null,
    val query: String = "",
    val category: EventCategory? = null,
    val locationFilter: LocationFilter = LocationFilter.ALL,
    val featuredEvents: List<CommunityEvent> = emptyList(),
    val upcomingEvents: List<CommunityEvent> = emptyList(),
    val interestedEventIds: Set<String> = emptySet(),
    /** Distancia de cada evento al usuario, en km. */
    val distancesKm: Map<String, Double> = emptyMap()
) {
    val userFirstName: String get() = user?.firstName.orEmpty()
    val city: String get() = user?.city.orEmpty()
    val hasResults: Boolean get() = featuredEvents.isNotEmpty() || upcomingEvents.isNotEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    observePublicEvents: ObservePublicEventsUseCase,
    observeInterests: ObserveInterestsUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val toggleInterestUseCase: ToggleInterestUseCase
) : ViewModel() {
    // Solo los eventos verificados (públicos)
    private var publicEvents: List<CommunityEvent> = observePublicEvents().value

    private val _uiState = MutableStateFlow(
        HomeUiState(
            user = observeCurrentUser().value,
            interestedEventIds = observeInterests().value.idsOf(observeCurrentUser().value)
        ).filtered(publicEvents)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Refleja los cambios hechos desde otras pantallas (asistencia, "Me interesa", perfil)
        viewModelScope.launch {
            combine(observePublicEvents(), observeInterests(), observeCurrentUser(), ::Triple).collect { (events, interests, user) ->
                publicEvents = events
                _uiState.update { it.copy(user = user, interestedEventIds = interests.idsOf(user)).filtered(events) }
            }
        }
    }

    // Los filtros se aplican al momento para que el campo de búsqueda responda sin retraso
    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered(publicEvents) }

    fun selectCategory(category: EventCategory?) = _uiState.update { it.copy(category = category).filtered(publicEvents) }

    fun selectLocationFilter(filter: LocationFilter) = _uiState.update { it.copy(locationFilter = filter).filtered(publicEvents) }

    fun toggleInterest(eventId: String) {
        val user = observeCurrentUser().value ?: return
        // El feed se actualiza solo al cambiar el conteo; si falla, el corazón queda como estaba
        viewModelScope.launch { runCatching { toggleInterestUseCase(eventId, user.id) } }
    }

    companion object {
        const val FEATURED_COUNT = 2
        const val NEARBY_RADIUS_KM = 5

        private fun List<Interest>.idsOf(user: User?): Set<String> =
            filter { it.userId == user?.id }.map { it.eventId }.toSet()

        /** Destacados: los de más votos «Me interesa»; los empates se ordenan por fecha e id. */
        private fun HomeUiState.filtered(events: List<CommunityEvent>): HomeUiState {
            val search = query.normalizedForSearch()
            val distances = user?.let { u -> events.associate { it.id to u.location.distanceKmTo(it.location) } }.orEmpty()
            val userCity = user?.city?.let(SamplePlaces::cityNamed)
            val matching = events.filter { event ->
                (category == null || event.category == category) &&
                    (search.isEmpty() || event.title.normalizedForSearch().contains(search)) &&
                    when (locationFilter) {
                        LocationFilter.ALL -> true
                        LocationFilter.NEARBY -> (distances[event.id] ?: Double.MAX_VALUE) <= NEARBY_RADIUS_KM
                        LocationFilter.CITY -> userCity != null && SamplePlaces.cityForLocation(event.location) == userCity
                    }
            }
            val featured = matching.sortedWith(
                compareByDescending<CommunityEvent> { it.interestCount }.thenBy { it.startsAt }.thenBy { it.id }
            ).take(FEATURED_COUNT)
            val featuredIds = featured.map { it.id }.toSet()
            return copy(
                featuredEvents = featured,
                upcomingEvents = matching.filter { it.id !in featuredIds }.sortedBy { it.startsAt },
                distancesKm = distances
            )
        }
    }
}
