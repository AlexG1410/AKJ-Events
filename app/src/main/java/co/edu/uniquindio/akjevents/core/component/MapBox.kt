package co.edu.uniquindio.akjevents.core.component

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.Location
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapState
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions

/**
 * Pasos 5, 6 y 8 de la guía 16, adaptados a eventos. La pantalla entrega los eventos filtrados
 * y decide qué hacer con los toques. No consulta repositorios ni solicita ubicación del dispositivo.
 * [centerRequest] permite recentrar incluso si las coordenadas no han cambiado.
 */
@Composable
fun MapBox(
    center: Location,
    modifier: Modifier = Modifier,
    zoom: Double = 13.5,
    events: List<CommunityEvent> = emptyList(),
    centerRequest: Int = 0,
    mapState: MapState = rememberMapState(),
    onMarkerClick: (String) -> Unit = {},
    onMapClick: () -> Unit = {},
    selectedPoint: Location? = null,
    onPointClick: (Location) -> Unit = {},
    onUserLocation: (Location) -> Unit = {},
    interactive: Boolean = true,
    showMyLocationButton: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    var locationRequest by remember { mutableIntStateOf(0) }
    var locationReceived by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val locationHelp = stringResource(R.string.map_location_unavailable)
    val permission = rememberLocationPermissionState()
    val currentOnUserLocation by rememberUpdatedState(onUserLocation)
    LaunchedEffect(locationRequest) {
        if (locationRequest > 0) {
            delay(15_000)
            if (!locationReceived) snackbar.showSnackbar(locationHelp)
        }
    }
    val viewport = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(center.longitude, center.latitude))
            zoom(zoom)
        }
    }
    // El estado inicial de rememberMapViewportState no mueve la cámara al cambiar sus parámetros.
    LaunchedEffect(center, zoom, centerRequest) {
        viewport.setCameraOptions {
            center(Point.fromLngLat(center.longitude, center.latitude))
            zoom(zoom)
        }
    }
    val marker = rememberIconImage(
        key = R.drawable.event_map_marker,
        painter = painterResource(R.drawable.event_map_marker)
    )
    val eventsById = remember(events) { events.associateBy { it.id } }
    val annotations = remember(events) {
        events.distinctBy { it.id }.map { event ->
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(event.location.longitude, event.location.latitude))
                .withData(JsonPrimitive(event.id))
        }
    }
    val clusterConfig = remember {
        AnnotationConfig(
            annotationSourceOptions = AnnotationSourceOptions(
                clusterOptions = ClusterOptions(clusterRadius = 60L)
            )
        )
    }

    Box(modifier) {
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = viewport,
            mapState = mapState,
            onMapClickListener = { point ->
                onMapClick()
                onPointClick(Location(point.latitude(), point.longitude()))
                true
            }
        ) {
            // El mapa interactivo tiene prioridad sobre el scroll de su contenedor.
            DisposableMapEffect(interactive) { mapView ->
                if (interactive) mapView.setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> view.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                            if (event.actionMasked == MotionEvent.ACTION_UP) view.performClick()
                        }
                    }
                    false
                }
                onDispose { mapView.setOnTouchListener(null) }
            }
            DisposableMapEffect(permission.hasPermission, interactive) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck()
                    enabled = permission.hasPermission && interactive
                }
                onDispose { mapView.location.updateSettings { enabled = false } }
            }
            if (permission.hasPermission && interactive && locationRequest > 0) {
                DisposableMapEffect(locationRequest) { mapView ->
                    var delivered = false
                    val listener = OnIndicatorPositionChangedListener { point ->
                        if (!delivered) {
                            delivered = true
                            locationReceived = true
                            viewport.setCameraOptions { center(point); zoom(16.0) }
                            currentOnUserLocation(Location(point.latitude(), point.longitude()))
                        }
                    }
                    mapView.location.addOnIndicatorPositionChangedListener(listener)
                    onDispose { mapView.location.removeOnIndicatorPositionChangedListener(listener) }
                }
            }
            selectedPoint?.let { point ->
                PointAnnotation(point = Point.fromLngLat(point.longitude, point.latitude)) {
                    iconImage = marker
                }
            }
            if (annotations.isNotEmpty()) {
                PointAnnotationGroup(annotations = annotations, annotationConfig = clusterConfig) {
                    iconImage = marker
                    interactionsState.onClicked { annotation ->
                        val id = annotation.getData()?.asString
                        if (id != null && id in eventsById) {
                            onMarkerClick(id)
                            true
                        } else false
                    }
                }
            }
        }
        if (!interactive) {
            Box(Modifier.matchParentSize().pointerInput(Unit) {})
        }
        if (interactive && showMyLocationButton) {
            LocationButton(permission) {
                locationReceived = false
                locationRequest++
            }
        }
        // Mantener libres el logotipo y la atribución de Mapbox en la zona inferior izquierda.
        content()
        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomEnd))
    }
}
