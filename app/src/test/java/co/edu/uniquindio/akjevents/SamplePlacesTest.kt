package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.Location
import org.junit.Assert.assertEquals
import org.junit.Test

class SamplePlacesTest {
    @Test
    fun knownPlacesKeepTheirCatalogCityEvenIfAnotherCenterIsCloser() {
        SamplePlaces.places.forEach { place ->
            assertEquals(place.city, SamplePlaces.cityForLocation(place.location).name)
        }
    }

    @Test
    fun unknownPointsUseTheNearestCityAsALocalApproximation() {
        val point = Location(4.5340, -75.6812)

        assertEquals("Armenia, Quindío", SamplePlaces.cityForLocation(point).name)
    }
}
