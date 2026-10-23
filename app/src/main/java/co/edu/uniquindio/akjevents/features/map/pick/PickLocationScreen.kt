package co.edu.uniquindio.akjevents.features.map.pick

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.CenterPin
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.SimulatedMap
import co.edu.uniquindio.akjevents.core.component.Tag
import co.edu.uniquindio.akjevents.core.component.rememberSimulatedMapState
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.domain.model.Location
import kotlinx.coroutines.launch
import java.util.Locale

private val SearchStyle = FormFieldStyle(
    height = 52.dp,
    shape = RoundedCornerShape(16.dp),
    containerColor = Color.White,
    borderColor = Color.Transparent,
    focusedBorderColor = Terracotta,
    textStyle = TextStyle(fontSize = 15.sp, color = WarmText),
    placeholderColor = MutedText.copy(alpha = 0.7f),
    iconColor = Terracotta,
    iconSize = 22.dp,
    iconStartPadding = 14.dp,
    textStartPadding = 48.dp
)

/** Mockup 09: el usuario mueve el mapa y el pin central marca el punto elegido. */
@Composable
fun PickLocationScreen(
    initial: PickedLocation,
    onBack: () -> Unit,
    onConfirm: (PickedLocation) -> Unit,
    viewModel: PickLocationViewModel = viewModel { PickLocationViewModel(initial) }
) {
    val state by viewModel.uiState.collectAsState()
    val mapState = rememberSimulatedMapState(center = initial.location, latSpan = 0.03)
    val clipboard = LocalClipboard.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // El punto elegido sigue al centro del mapa
    LaunchedEffect(mapState) {
        snapshotFlow { mapState.center }.collect(viewModel::onCenterChange)
    }

    Scaffold(
        containerColor = WarmBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                title = "Seleccionar ubicación",
                kicker = "Mapa",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Tag(
                    text = state.city.substringBefore(","),
                    icon = Icons.Outlined.LocationCity,
                    container = SecondaryFixed,
                    content = OnSecondaryFixedVariant
                )
            }

            FormTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "Buscar parque, plaza o lugar...",
                leadingIcon = Icons.Outlined.Search,
                style = SearchStyle,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    state.suggestions.firstOrNull()?.let { mapState.moveTo(it.location) }
                }),
                trailingContent = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Borrar búsqueda", tint = MutedText)
                        }
                    }
                } else null,
                modifier = Modifier.padding(horizontal = 16.dp).shadow(2.dp, RoundedCornerShape(16.dp))
            )

            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.suggestions, key = { it.name }) { place ->
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            mapState.moveTo(place.location)
                        },
                        shape = CircleShape,
                        color = if (place.name == state.placeName) PrimaryFixed else SurfaceContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Place, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                            Text(place.name, color = WarmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            SimulatedMap(
                state = mapState,
                onTap = { mapState.moveTo(it) },
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                CenterPin(label = "Ubicación seleccionada")
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .shadow(2.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.PanTool, contentDescription = null, tint = WarmText, modifier = Modifier.size(16.dp))
                    Text("Mueve el mapa o toca un punto", color = WarmText, fontSize = 12.sp)
                }
            }

            SelectionCard(
                state = state,
                onCopy = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Coordenadas", state.selected.coordinatesText())))
                        snackbarHostState.showSnackbar("Coordenadas copiadas")
                    }
                },
                onConfirm = { onConfirm(viewModel.result()) }
            )
        }
    }
}

private fun Location.coordinatesText() =
    "Lat: ${"%.4f".format(Locale.US, latitude)}, Lon: ${"%.4f".format(Locale.US, longitude)}"

@Composable
private fun SelectionCard(state: PickLocationUiState, onCopy: () -> Unit, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).background(PrimaryFixed, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Terracotta)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("PUNTO SELECCIONADO", color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(state.placeName ?: state.address, color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(state.city, color = MutedText, fontSize = 12.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text("COORDENADAS GPS", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(state.selected.coordinatesText(), color = WarmText, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar coordenadas", tint = Terracotta, modifier = Modifier.size(20.dp))
            }
        }
        if (state.placeName != null) {
            Row(
                modifier = Modifier.fillMaxWidth().background(SecondaryFixed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(18.dp))
                Text("Lugar público reconocido, accesible para la comunidad.", color = OnSecondaryFixedVariant, fontSize = 12.sp)
            }
        }
        PrimaryButton(
            text = "Confirmar ubicación",
            onClick = onConfirm,
            leadingIcon = Icons.Outlined.CheckCircle,
            containerColor = Terracotta
        )
    }
}
