package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.features.map.pick.PickLocationViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class PickLocationViewModelTest {
    @Test
    fun openingPickerPreservesInitialCoordinatesAndAddress() {
        val initial = PickedLocation("Dirección guardada", Location(4.54, -75.67))
        val viewModel = PickLocationViewModel(initial, TestResourceProvider)
        assertEquals(initial, viewModel.result())
    }

    @Test
    fun reselectionReturnsLatestCoordinatesWithoutSwappingLatitudeAndLongitude() {
        val viewModel = PickLocationViewModel(
            PickedLocation("Anterior", Location(4.54, -75.67)), TestResourceProvider
        )
        viewModel.onCenterChange(Location(4.55, -75.66))
        val latest = Location(4.5296, -75.6431)
        viewModel.onCenterChange(latest)
        viewModel.onQueryChange("Parque")
        assertEquals(latest, viewModel.result().location)
        assertEquals("Parque Bolívar de Calarcá, Calarcá, Quindío", viewModel.result().address)
    }
}
