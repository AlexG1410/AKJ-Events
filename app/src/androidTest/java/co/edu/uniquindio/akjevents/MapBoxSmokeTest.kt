package co.edu.uniquindio.akjevents

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.compose.ui.geometry.Offset
import co.edu.uniquindio.akjevents.core.component.MapBox
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import com.mapbox.maps.extension.compose.rememberMapState
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Prueba aislada: usa Mapbox y el token local, pero no consulta ni escribe en Firebase. */
class MapBoxSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tappingMapReturnsCoordinatesAndAllowsReselection() {
        val loaded = AtomicBoolean(false)
        val selected = AtomicReference<Location?>(null)
        compose.setContent {
            val state = rememberMapState()
            LaunchedEffect(state) { state.mapLoadedEvents.collect { loaded.set(true) } }
            MapBox(
                center = Location(4.5339, -75.6811), mapState = state,
                showMyLocationButton = false,
                onPointClick = { selected.set(it) },
                modifier = Modifier.fillMaxSize().testTag("picker-map")
            )
        }
        compose.waitUntil(60_000) { loaded.get() }
        compose.onNodeWithTag("picker-map").performTouchInput { click(center) }
        compose.waitUntil(10_000) { selected.get() != null }
        val first = selected.get()!!
        assertEquals(4.5339, first.latitude, 0.001)
        assertEquals(-75.6811, first.longitude, 0.001)
        compose.onNodeWithTag("picker-map").performTouchInput { click(center + Offset(100f, -100f)) }
        compose.waitUntil(10_000) { selected.get() != first }
    }

    @Test
    fun readOnlyMapDoesNotSelectPoint() {
        val loaded = AtomicBoolean(false)
        val selected = AtomicBoolean(false)
        compose.setContent {
            val state = rememberMapState()
            LaunchedEffect(state) { state.mapLoadedEvents.collect { loaded.set(true) } }
            MapBox(
                center = Location(4.5339, -75.6811), mapState = state,
                interactive = false, onPointClick = { selected.set(true) },
                modifier = Modifier.fillMaxSize().testTag("detail-map")
            )
        }
        compose.waitUntil(60_000) { loaded.get() }
        compose.onNodeWithTag("detail-map").performTouchInput { click(center) }
        compose.waitForIdle()
        assertEquals(false, selected.get())
    }

    @Test
    fun loadsRealMapAndSelectsEventMarker() {
        val loaded = AtomicBoolean(false)
        val selected = AtomicReference<String?>(null)
        val location = Location(4.5339, -75.6811)
        val start = LocalDateTime.now().plusDays(1)
        val event = CommunityEvent(
            id = "mapbox-smoke-test", organizerId = "local-test", title = "Evento de prueba local",
            category = EventCategory.CULTURE, description = "No se guarda en Firebase",
            address = "Quindío", location = location, imageUrls = listOf("local-test"),
            startsAt = start, endsAt = start.plusHours(1), status = EventStatus.VERIFIED
        )
        compose.setContent {
            val state = rememberMapState()
            LaunchedEffect(state) {
                state.mapLoadedEvents.collect { loaded.set(true) }
            }
            MapBox(
                center = location,
                events = listOf(event),
                mapState = state,
                onMarkerClick = { selected.set(it) },
                modifier = Modifier.fillMaxSize().testTag("real-map")
            )
        }
        compose.waitUntil(timeoutMillis = 60_000) { loaded.get() }
        compose.onNodeWithTag("real-map").performTouchInput { click(center) }
        compose.waitUntil(timeoutMillis = 10_000) { selected.get() != null }
        assertEquals(event.id, selected.get())
    }
}
