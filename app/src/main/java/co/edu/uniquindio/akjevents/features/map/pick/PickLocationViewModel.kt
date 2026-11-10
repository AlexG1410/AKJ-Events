package co.edu.uniquindio.akjevents.features.map.pick

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.data.demo.Place
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.akjevents.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AddressLookupState { SAVED, LOADING, FOUND, NOT_FOUND, ERROR, MANUAL, COORDINATES }

data class PickLocationUiState(
    val query: String = "",
    val suggestions: List<Place> = SamplePlaces.places,
    val selected: Location,
    /** Nombre del lugar conocido bajo el pin, si lo hay. */
    val placeName: String? = null,
    val address: String = "",
    val city: String = "",
    val addressLookup: AddressLookupState = AddressLookupState.SAVED
)

/**
 * Conserva el punto elegido por toque, lugar de referencia o ubicación del dispositivo.
 * La búsqueda usa lugares de referencia locales en lugar de un servicio de geocodificación.
 */
@HiltViewModel
class PickLocationViewModel(
    initial: PickedLocation,
    private val resources: ResourceProvider,
    private val reverseGeocode: ReverseGeocodeUseCase
) : ViewModel() {

    /** Hilt lee el punto inicial de la ruta [MainRoutes.PickLocation]. */
    @Inject
    constructor(savedStateHandle: SavedStateHandle, resources: ResourceProvider, reverseGeocode: ReverseGeocodeUseCase) : this(
        savedStateHandle.toRoute<MainRoutes.PickLocation>().let { PickedLocation(it.address, Location(it.latitude, it.longitude), it.city) },
        resources,
        reverseGeocode
    )

    private val _uiState = MutableStateFlow(
        PickLocationUiState(
            selected = initial.location,
            address = initial.address.ifBlank { coordinatesAddress(initial.location) },
            city = initial.city.orEmpty()
        )
    )
    val uiState: StateFlow<PickLocationUiState> = _uiState.asStateFlow()
    private var lookup: Job? = null
    private var lookupVersion = 0

    init {
        // No reemplazar una dirección ya guardada o escrita al abrir de nuevo el selector.
        if (initial.address.isBlank()) retryAddress()
    }

    fun onQueryChange(query: String) = _uiState.update { state ->
        val search = query.normalizedForSearch()
        state.copy(
            query = query,
            suggestions = if (search.isEmpty()) SamplePlaces.places else SamplePlaces.places.filter {
                it.name.normalizedForSearch().contains(search) || it.city.normalizedForSearch().contains(search)
            }
        )
    }

    /** El usuario eligió otro punto; mover solamente la cámara no cambia esta selección. */
    fun onCenterChange(location: Location) {
        if (location == _uiState.value.selected) return
        _uiState.update {
            it.copy(selected = location, address = coordinatesAddress(location), city = "", placeName = null)
        }
        retryAddress()
    }

    fun onPlaceSelected(place: Place) {
        _uiState.update {
            it.copy(selected = place.location, address = "${place.address}, ${place.city}", city = place.city, placeName = place.name)
        }
        retryAddress()
    }

    fun retryAddress() {
        lookup?.cancel()
        val version = ++lookupVersion
        val location = _uiState.value.selected
        _uiState.update { it.copy(addressLookup = AddressLookupState.LOADING) }
        lookup = viewModelScope.launch {
            // Evita solicitudes por cada toque de una selección rápida. Arrastrar no llama este método.
            delay(350)
            try {
                val address = reverseGeocode(location)
                ensureActive()
                if (version != lookupVersion) return@launch
                _uiState.update {
                    if (address == null || address.address.isBlank()) it.copy(addressLookup = AddressLookupState.NOT_FOUND)
                    else it.copy(address = address.address, city = address.city, addressLookup = AddressLookupState.FOUND)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (version == lookupVersion) _uiState.update { it.copy(addressLookup = AddressLookupState.ERROR) }
            }
        }
    }

    fun onManualAddress(address: String, city: String) {
        cancelLookup()
        _uiState.update {
            it.copy(address = address.trim().ifBlank { coordinatesAddress(it.selected) }, city = city.trim(), placeName = null, addressLookup = AddressLookupState.MANUAL)
        }
    }

    fun useCoordinates() {
        cancelLookup()
        _uiState.update { it.copy(address = coordinatesAddress(it.selected), city = "", placeName = null, addressLookup = AddressLookupState.COORDINATES) }
    }

    private fun cancelLookup() {
        lookupVersion++
        lookup?.cancel()
    }

    fun result(): PickedLocation = _uiState.value.let { PickedLocation(it.address, it.selected, it.city) }

    private fun coordinatesAddress(location: Location): String = resources.getString(
        R.string.pick_coordinate_address,
        "%.5f, %.5f".format(Locale.US, location.latitude, location.longitude)
    )
}
