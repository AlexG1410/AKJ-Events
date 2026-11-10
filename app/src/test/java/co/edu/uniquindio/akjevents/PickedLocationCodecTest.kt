package co.edu.uniquindio.akjevents

import androidx.lifecycle.SavedStateHandle
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.PickedLocationCodec
import co.edu.uniquindio.akjevents.domain.model.Location
import org.junit.Assert.*
import org.junit.Test

class PickedLocationCodecTest {
    @Test fun jsonPreservesSpecialCharactersAndCity() {
        val picked = PickedLocation("Calle 10 | Entrada \"Norte\"\n#2", Location(4.5, -75.6), "Bogotá, D. C.")
        assertEquals(picked, PickedLocationCodec.decode(PickedLocationCodec.encode(picked)))
    }

    @Test fun legacyResultsRemainReadableIncludingPipesInAddress() {
        assertEquals(
            PickedLocation("Dirección | local", Location(4.5, -75.6)),
            PickedLocationCodec.decode("4.5|-75.6|Dirección | local")
        )
    }

    @Test fun emptyCityIsNotMistakenForALegacyResult() {
        val picked = PickedLocation("Coordenadas", Location(4.5, -75.6), "")
        assertEquals("", PickedLocationCodec.decode(PickedLocationCodec.encode(picked))?.city)
    }

    @Test fun malformedOrInvalidCoordinatesAreRejected() {
        listOf("invalid", "NaN|0|x", "91|0|x", "0|-181|x", "{bad json}", "{\"latitude\":91,\"longitude\":0,\"address\":\"x\"}")
            .forEach { assertNull(PickedLocationCodec.decode(it)) }
    }

    @Test fun resultFlowCanDeliverASecondSelectionAfterFirstWasConsumed() {
        val handle = SavedStateHandle()
        val result = handle.getStateFlow<String?>("picked_location", null)
        val first = PickedLocation("Primera", Location(4.5, -75.6), "Armenia")
        val second = PickedLocation("Segunda", Location(4.7, -74.0), "Bogotá")
        handle["picked_location"] = PickedLocationCodec.encode(first)
        assertEquals(first, PickedLocationCodec.decode(result.value!!))
        handle.set<String?>("picked_location", null)
        assertNull(result.value)
        handle["picked_location"] = PickedLocationCodec.encode(second)
        assertEquals(second, PickedLocationCodec.decode(result.value!!))
    }
}
