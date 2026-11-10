package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import co.edu.uniquindio.akjevents.domain.usecase.location.ReverseGeocodeUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ReverseGeocodeUseCaseTest {
    @Test fun invalidCoordinatesNeverReachProvider() = runTest {
        var calls = 0
        val useCase = ReverseGeocodeUseCase(object : GeocodingRepository {
            override suspend fun reverseGeocode(location: Location): GeocodedAddress? { calls++; return null }
        })
        listOf(Location(Double.NaN, 0.0), Location(91.0, 0.0), Location(0.0, 181.0)).forEach {
            try { useCase(it); fail("Invalid coordinate accepted") } catch (_: IllegalArgumentException) { }
        }
        assertEquals(0, calls)
    }
}
