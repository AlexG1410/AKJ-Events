package co.edu.uniquindio.akjevents.features.map.pick

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.data.demo.Place
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PickLocationUiState(
    val query: String = "",
    val suggestions: List<Place> = SamplePlaces.places,
    val selected: Location,
    /** Nombre del lugar conocido bajo el pin, si lo hay. */
    val placeName: String? = null,
    val address: String = "",
    val city: String = ""
)

/**
 * Elige un punto moviendo el mapa simulado: el punto seleccionado es el centro del mapa.
 * La búsqueda usa lugares de referencia locales en lugar de un servicio de geocodificación.
 */
class PickLocationViewModel(initial: PickedLocation) : ViewModel() {
    private val _uiState = MutableStateFlow(
        PickLocationUiState(selected = initial.location).resolved(initialAddress = initial.address.ifBlank { null })
    )
    val uiState: StateFlow<PickLocationUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) = _uiState.update { state ->
        val search = query.normalizedForSearch()
        state.copy(
            query = query,
            suggestions = if (search.isEmpty()) SamplePlaces.places else SamplePlaces.places.filter {
                it.name.normalizedForSearch().contains(search) || it.city.normalizedForSearch().contains(search)
            }
        )
    }

    /** El mapa se movió: el nuevo centro es el punto elegido. */
    fun onCenterChange(location: Location) = _uiState.update { it.copy(selected = location).resolved() }

    fun result(): PickedLocation = _uiState.value.let { PickedLocation(it.address, it.selected) }

    private fun PickLocationUiState.resolved(initialAddress: String? = null): PickLocationUiState {
        val place = SamplePlaces.places.minBy { it.location.distanceKmTo(selected) }
            .takeIf { it.location.distanceKmTo(selected) <= SNAP_RADIUS_KM }
        val city = SamplePlaces.nearestCity(selected).name
        return copy(
            placeName = place?.name,
            address = initialAddress ?: place?.let { "${it.address}, ${it.city}" } ?: "Punto en el mapa, $city",
            city = place?.city ?: city
        )
    }

    private companion object {
        // Distancia a la que el pin "reconoce" un lugar conocido
        const val SNAP_RADIUS_KM = 0.25
    }
}
