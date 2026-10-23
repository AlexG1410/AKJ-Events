package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.widthIn
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.Location

/*
 * Mapa simulado del Eje Cafetero dibujado con Canvas. En esta fase no se integra un SDK de mapas
 * (Mapbox o Google Maps llegarán con el backend), pero el mapa trabaja con latitud y longitud reales:
 * permite moverse, acercarse, ubicar marcadores y elegir un punto.
 */

private val MapBackground = Color(0xFFF4EFE6)
private val MapGreen = Color(0xFFDDEBD9)
private val MapWater = Color(0xFFB9DCEB)
private val MapRoad = Color(0xFFEBDDD3)
private val MapStreet = Color(0xFFE9E1D6)
private val MapLabel = Color(0xFF9A8A80)

private const val MIN_LAT_SPAN = 0.008
private const val MAX_LAT_SPAN = 0.6

/** Marcador de un evento en el mapa. */
data class MapMarker(
    val id: String,
    val location: Location,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val contentColor: Color = Color.White
)

/** Centro y acercamiento del mapa; [latSpan] son los grados de latitud que caben a lo alto. */
@Stable
class SimulatedMapState(center: Location, latSpan: Double) {
    var center by mutableStateOf(center)
    var latSpan by mutableStateOf(latSpan)
        private set

    fun zoomIn() {
        latSpan = (latSpan / 1.6).coerceAtLeast(MIN_LAT_SPAN)
    }

    fun zoomOut() {
        latSpan = (latSpan * 1.6).coerceAtMost(MAX_LAT_SPAN)
    }

    fun moveTo(location: Location) {
        center = Location(location.latitude.coerceIn(4.30, 5.20), location.longitude.coerceIn(-76.0, -75.35))
    }
}

@Composable
fun rememberSimulatedMapState(center: Location, latSpan: Double = 0.04): SimulatedMapState =
    remember { SimulatedMapState(center, latSpan) }

/** Conversión entre coordenadas y píxeles para el tamaño actual del mapa. */
private class Projection(val center: Location, val latSpan: Double, val width: Float, val height: Float) {
    val lngSpan = latSpan * width / height

    fun toOffset(location: Location) = Offset(
        x = ((location.longitude - (center.longitude - lngSpan / 2)) / lngSpan * width).toFloat(),
        y = (((center.latitude + latSpan / 2) - location.latitude) / latSpan * height).toFloat()
    )

    fun toLocation(offset: Offset) = Location(
        latitude = center.latitude + latSpan / 2 - offset.y / height * latSpan,
        longitude = center.longitude - lngSpan / 2 + offset.x / width * lngSpan
    )

    fun isVisible(offset: Offset, margin: Float = 80f) =
        offset.x in -margin..(width + margin) && offset.y in -margin..(height + margin)

    /** Píxeles que ocupa un kilómetro (1° de latitud ≈ 111 km). */
    val pxPerKm: Float get() = (height / (latSpan * 111.0)).toFloat()
}

@Composable
fun SimulatedMap(
    state: SimulatedMapState,
    modifier: Modifier = Modifier,
    markers: List<MapMarker> = emptyList(),
    selectedMarkerId: String? = null,
    onMarkerClick: (String) -> Unit = {},
    userLocation: Location? = null,
    onTap: ((Location) -> Unit)? = null,
    interactive: Boolean = true,
    showControls: Boolean = interactive,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(MapBackground)) {
        val density = LocalDensity.current
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }
        val projection = Projection(state.center, state.latSpan, width, height)

        val gestures = if (interactive) {
            Modifier
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        val current = Projection(state.center, state.latSpan, size.width.toFloat(), size.height.toFloat())
                        state.moveTo(
                            Location(
                                state.center.latitude + drag.y / size.height * state.latSpan,
                                state.center.longitude - drag.x / size.width * current.lngSpan
                            )
                        )
                    }
                }
                .pointerInput(onTap) {
                    detectTapGestures { offset ->
                        val current = Projection(state.center, state.latSpan, size.width.toFloat(), size.height.toFloat())
                        onTap?.invoke(current.toLocation(offset))
                    }
                }
        } else Modifier

        Canvas(modifier = Modifier.fillMaxSize().then(gestures)) {
            drawBaseMap(projection, textMeasurer)
        }

        markers.forEach { marker ->
            val position = projection.toOffset(marker.location)
            if (projection.isVisible(position)) {
                MarkerPin(
                    marker = marker,
                    isSelected = marker.id == selectedMarkerId,
                    onClick = { onMarkerClick(marker.id) },
                    modifier = Modifier.placeAt(position)
                )
            }
        }

        userLocation?.let { location ->
            val position = projection.toOffset(location)
            if (projection.isVisible(position)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.placeAt(position, centerVertically = true)
                ) {
                    Box(
                        modifier = Modifier.size(28.dp).background(Sage.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.size(14.dp).border(3.dp, Color.White, CircleShape).background(Sage, CircleShape))
                    }
                }
            }
        }

        if (showControls) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (userLocation != null) {
                    MapButton(Icons.Outlined.MyLocation, "Centrar en mi ubicación", Terracotta) { state.moveTo(userLocation) }
                }
                MapButton(Icons.Outlined.Add, "Acercar", WarmText, state::zoomIn)
                MapButton(Icons.Outlined.Remove, "Alejar", WarmText, state::zoomOut)
            }
        }

        overlay()
    }
}

/** Ubica el elemento con su punta inferior (o su centro) sobre [position]. */
private fun Modifier.placeAt(position: Offset, centerVertically: Boolean = false) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(placeable.width, placeable.height) {
        val y = if (centerVertically) position.y - placeable.height / 2 else position.y - placeable.height
        placeable.place((position.x - placeable.width / 2).toInt(), y.toInt())
    }
}

private val PointerShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2, size.height)
    close()
}

@Composable
private fun MarkerPin(marker: MapMarker, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (isSelected) Terracotta else marker.color
    val content = if (isSelected) Color.White else marker.contentColor
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Surface(onClick = onClick, shape = CircleShape, color = color, shadowElevation = if (isSelected) 6.dp else 2.dp) {
            Row(
                modifier = Modifier.padding(horizontal = if (isSelected) 12.dp else 10.dp, vertical = if (isSelected) 6.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(marker.icon, contentDescription = null, tint = content, modifier = Modifier.size(if (isSelected) 18.dp else 14.dp))
                Text(
                    marker.label,
                    color = content,
                    fontSize = if (isSelected) 13.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp)
                )
            }
        }
        Box(Modifier.size(width = 12.dp, height = 7.dp).background(color, PointerShape))
    }
}

@Composable
private fun MapButton(icon: ImageVector, description: String, tint: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = Color.White, shadowElevation = 3.dp, modifier = Modifier.size(44.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(22.dp))
        }
    }
}

/** Pin fijo en el centro del mapa para elegir una ubicación moviendo el mapa (mockup 09). */
@Composable
fun BoxScope.CenterPin(label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.align(Alignment.Center).layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            // La punta del pin queda justo en el centro del mapa
            layout(placeable.width, placeable.height) { placeable.place(0, -placeable.height / 2) }
        }
    ) {
        Text(
            label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .background(WarmText.copy(alpha = 0.85f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
        Box(
            modifier = Modifier.padding(top = 6.dp).size(44.dp).shadow(6.dp, CircleShape).background(Terracotta, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(14.dp).border(3.dp, Color.White, CircleShape))
        }
        Box(Modifier.size(width = 4.dp, height = 10.dp).background(Terracotta))
    }
}

// Trazos aproximados de vías y del río Quindío, en coordenadas reales
private val MainRoads: List<List<Location>> = listOf(
    // Avenida Bolívar
    listOf(Location(4.505, -75.700), Location(4.533, -75.681), Location(4.551, -75.660), Location(4.575, -75.652)),
    // Autopista del Café hacia Pereira
    listOf(Location(4.551, -75.660), Location(4.619, -75.636), Location(4.675, -75.658), Location(4.740, -75.680), Location(4.813, -75.696)),
    // Vía a Calarcá
    listOf(Location(4.535, -75.672), Location(4.531, -75.655), Location(4.530, -75.643)),
    // Vía a Salento
    listOf(Location(4.575, -75.652), Location(4.610, -75.610), Location(4.638, -75.570), Location(4.638, -75.487)),
    // Vía a Montenegro y Quimbaya
    listOf(Location(4.540, -75.690), Location(4.566, -75.751), Location(4.623, -75.763)),
    // Vía a La Tebaida
    listOf(Location(4.520, -75.700), Location(4.485, -75.745), Location(4.453, -75.788))
)

private val RiverQuindio = listOf(
    Location(4.660, -75.540), Location(4.620, -75.585), Location(4.585, -75.625),
    Location(4.548, -75.643), Location(4.515, -75.658), Location(4.470, -75.690), Location(4.420, -75.720)
)

/** Parques y zonas verdes: centro y radio en km. */
private val GreenAreas: List<Pair<Location, Double>> = listOf(
    Location(4.5511, -75.6598) to 0.35,
    Location(4.5382, -75.6731) to 0.12,
    Location(4.5452, -75.6672) to 0.18,
    Location(4.5186, -75.6425) to 0.6,
    Location(4.6380, -75.4870) to 1.6,
    Location(4.5208, -75.6858) to 0.2
)

private fun DrawScope.drawBaseMap(projection: Projection, textMeasurer: TextMeasurer) {
    // Cuadrícula de calles en los cascos urbanos, solo cuando hay suficiente acercamiento
    if (projection.latSpan < 0.12) {
        SamplePlaces.cities.forEach { city ->
            val step = 0.004
            for (i in -6..6) {
                val lat = city.location.latitude + i * step
                drawLine(MapStreet, projection.toOffset(Location(lat, city.location.longitude - 0.025)), projection.toOffset(Location(lat, city.location.longitude + 0.025)), strokeWidth = 3f)
                val lng = city.location.longitude + i * step
                drawLine(MapStreet, projection.toOffset(Location(city.location.latitude - 0.025, lng)), projection.toOffset(Location(city.location.latitude + 0.025, lng)), strokeWidth = 3f)
            }
        }
    }

    GreenAreas.forEach { (center, radiusKm) ->
        drawCircle(MapGreen, radius = (radiusKm * projection.pxPerKm).toFloat().coerceAtLeast(6f), center = projection.toOffset(center))
    }

    drawPolyline(RiverQuindio, projection, MapWater, 10f)
    MainRoads.forEach { drawPolyline(it, projection, MapRoad, 14f) }
    MainRoads.forEach { drawPolyline(it, projection, Color.White, 6f) }

    SamplePlaces.cities.forEach { city ->
        val position = projection.toOffset(city.location)
        if (projection.isVisible(position, margin = 0f)) {
            val name = city.name.substringBefore(",").uppercase()
            val layout = textMeasurer.measure(name, TextStyle(color = MapLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            drawText(layout, topLeft = Offset(position.x - layout.size.width / 2, position.y + 18f))
        }
    }

    // Nombres de lugares de referencia cuando se está cerca
    if (projection.latSpan < 0.05) {
        SamplePlaces.places.forEach { place ->
            val position = projection.toOffset(place.location)
            if (projection.isVisible(position, margin = 0f)) {
                drawCircle(MutedText.copy(alpha = 0.35f), radius = 5f, center = position)
                val layout = textMeasurer.measure(place.name, TextStyle(color = MapLabel.copy(alpha = 0.9f), fontSize = 9.sp))
                drawText(layout, topLeft = Offset(position.x + 8f, position.y - layout.size.height / 2))
            }
        }
    }
}

private fun DrawScope.drawPolyline(points: List<Location>, projection: Projection, color: Color, width: Float) {
    val path = Path()
    points.forEachIndexed { index, location ->
        val offset = projection.toOffset(location)
        if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
    }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round))
}
