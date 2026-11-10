package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import co.edu.uniquindio.akjevents.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.akjevents.features.map.pick.AddressLookupState
import co.edu.uniquindio.akjevents.features.map.pick.PickLocationViewModel
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PickLocationViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    private val initial = PickedLocation("Dirección guardada", Location(4.54, -75.67), "Armenia, Quindío")
    private val calls = mutableListOf<Location>()
    private var response: suspend (Location) -> GeocodedAddress? = { GeocodedAddress("Dirección encontrada", "Calarcá, Quindío") }
    private fun viewModel(start: PickedLocation = initial) = PickLocationViewModel(
        start, TestResourceProvider, ReverseGeocodeUseCase(object : GeocodingRepository {
            override suspend fun reverseGeocode(location: Location): GeocodedAddress? {
                calls += location
                return response(location)
            }
        })
    )

    @Test fun openingPickerPreservesInitialCoordinatesAddressAndCityWithoutQuerying() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(initial, vm.result())
        assertTrue(calls.isEmpty())
    }

    @Test fun emptyInitialAddressIsResolvedAutomatically() = runTest {
        val vm = viewModel(initial.copy(address = ""))
        assertEquals(AddressLookupState.LOADING, vm.uiState.value.addressLookup)
        advanceUntilIdle()
        assertEquals("Dirección encontrada", vm.result().address)
        assertEquals(listOf(initial.location), calls)
    }

    @Test fun reselectionReturnsLatestCoordinatesWithoutSwappingLatitudeAndLongitude() = runTest {
        val vm = viewModel()
        vm.onCenterChange(Location(4.55, -75.66))
        val latest = Location(4.5296, -75.6431)
        vm.onCenterChange(latest)
        vm.onQueryChange("Parque")
        advanceUntilIdle()
        assertEquals(listOf(latest), calls)
        assertEquals(PickedLocation("Dirección encontrada", latest, "Calarcá, Quindío"), vm.result())
        assertEquals(AddressLookupState.FOUND, vm.uiState.value.addressLookup)
    }

    @Test fun oldResponseCannotOverwriteNewSelectionEvenIfProviderIgnoresCancellation() = runTest {
        val first = Location(4.55, -75.66)
        val second = Location(4.5296, -75.6431)
        response = { point ->
            if (point == first) withContext(NonCancellable) { delay(2_000) }
            GeocodedAddress(if (point == first) "Anterior" else "Actual", "Ciudad")
        }
        val vm = viewModel()
        vm.onCenterChange(first)
        advanceTimeBy(400)
        vm.onCenterChange(second)
        advanceUntilIdle()
        assertEquals(second, vm.result().location)
        assertEquals("Actual", vm.result().address)
    }

    @Test fun failurePreservesCoordinatesAndRetryCanResolveAddress() = runTest {
        response = { throw IOException("offline") }
        val vm = viewModel()
        val selected = Location(37.422, -122.084)
        vm.onCenterChange(selected)
        advanceUntilIdle()
        assertEquals(AddressLookupState.ERROR, vm.uiState.value.addressLookup)
        assertEquals(selected, vm.result().location)
        assertEquals("", vm.result().city)
        assertTrue(vm.result().address.contains("37.42200, -122.08400"))

        response = { GeocodedAddress("Amphitheatre Parkway", "Mountain View, California") }
        vm.retryAddress()
        advanceUntilIdle()
        assertEquals("Mountain View, California", vm.result().city)
        assertEquals(selected, vm.result().location)
    }

    @Test fun noResultsDoNotInventANearbyColombianCity() = runTest {
        response = { null }
        val vm = viewModel()
        vm.onCenterChange(Location(37.422, -122.084))
        advanceUntilIdle()
        assertEquals(AddressLookupState.NOT_FOUND, vm.uiState.value.addressLookup)
        assertEquals("", vm.result().city)
    }

    @Test fun manualAddressCancelsPendingLookupWithoutMovingPoint() = runTest {
        response = { withContext(NonCancellable) { delay(2_000) }; GeocodedAddress("Remota", "Otra ciudad") }
        val vm = viewModel()
        val selected = Location(4.55, -75.66)
        vm.onCenterChange(selected)
        advanceTimeBy(400)
        vm.onManualAddress("  Entrada norte  ", "  Mi municipio  ")
        advanceUntilIdle()
        assertEquals(PickedLocation("Entrada norte", selected, "Mi municipio"), vm.result())
        assertEquals(AddressLookupState.MANUAL, vm.uiState.value.addressLookup)
    }

    @Test fun coordinatesOptionCancelsLookupAndDoesNotReusePreviousAddress() = runTest {
        val vm = viewModel()
        vm.onCenterChange(Location(4.55, -75.66))
        vm.useCoordinates()
        advanceUntilIdle()
        assertEquals(AddressLookupState.COORDINATES, vm.uiState.value.addressLookup)
        assertTrue(calls.isEmpty())
        assertEquals("", vm.result().city)
        assertTrue(vm.result().address.startsWith("Punto del mapa"))
    }

    @Test fun explicitLocalReferenceRemainsAnAvailableFallbackOnFailure() = runTest {
        response = { throw IOException("unavailable") }
        val place = SamplePlaces.places.first()
        val vm = viewModel()
        vm.onPlaceSelected(place)
        advanceUntilIdle()
        assertEquals(place.city, vm.result().city)
        assertEquals("${place.address}, ${place.city}", vm.result().address)
        assertEquals(AddressLookupState.ERROR, vm.uiState.value.addressLookup)
    }

    @Test fun coroutineCancellationIsNotReportedAsNetworkFailure() = runTest {
        response = { throw CancellationException("cancelled") }
        val vm = viewModel()
        vm.retryAddress()
        advanceUntilIdle()
        assertNotEquals(AddressLookupState.ERROR, vm.uiState.value.addressLookup)
        vm.useCoordinates()
    }
}
