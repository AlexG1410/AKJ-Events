package co.edu.uniquindio.akjevents.features.map.pick

import android.content.ClipData
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.MapBox
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.Tag
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
import co.edu.uniquindio.akjevents.data.demo.Place
import java.util.Locale
import kotlinx.coroutines.launch

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

/** Mockup 09 adaptado al toque para seleccionar de la guía 16; arrastrar solo mueve la cámara. */
@Composable
fun PickLocationScreen(
    initial: PickedLocation,
    onBack: () -> Unit,
    onConfirm: (PickedLocation) -> Unit,
    viewModel: PickLocationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var cameraCenter by remember(initial.location) { mutableStateOf(state.selected) }
    var centerRequest by remember { mutableIntStateOf(0) }
    var showAddressEditor by rememberSaveable { mutableStateOf(false) }
    fun select(location: Location) {
        viewModel.onCenterChange(location)
        cameraCenter = location
        centerRequest++
    }
    fun selectPlace(place: Place) {
        viewModel.onPlaceSelected(place)
        cameraCenter = place.location
        centerRequest++
    }
    val clipboard = LocalClipboard.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                title = stringResource(R.string.pick_title),
                kicker = stringResource(R.string.nav_map),
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Tag(
                    text = state.city.substringBefore(",").ifBlank { stringResource(R.string.pick_city_unknown) },
                    icon = Icons.Outlined.LocationCity,
                    container = SecondaryFixed,
                    content = OnSecondaryFixedVariant
                )
            }

            FormTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.pick_search_placeholder),
                leadingIcon = Icons.Outlined.Search,
                style = SearchStyle,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    state.suggestions.firstOrNull()?.let { selectPlace(it) }
                }),
                trailingContent = if (state.query.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.home_clear_search), tint = MutedText)
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
                            selectPlace(place)
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

            MapBox(
                center = cameraCenter,
                centerRequest = centerRequest,
                zoom = 16.0,
                selectedPoint = state.selected,
                onPointClick = viewModel::onCenterChange,
                onUserLocation = { select(it) },
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                        .shadow(2.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.PanTool, contentDescription = null, tint = WarmText, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.pick_hint), color = WarmText, fontSize = 12.sp)
                }
            }

            val coordinatesLabel = stringResource(R.string.pick_coordinates)
            val copiedMessage = stringResource(R.string.pick_coordinates_copied)
            SelectionCard(
                state = state,
                onRetry = viewModel::retryAddress,
                onUseCoordinates = viewModel::useCoordinates,
                onEditAddress = { showAddressEditor = true },
                onCopy = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(coordinatesLabel, state.selected.coordinatesText())))
                        snackbarHostState.showSnackbar(copiedMessage)
                    }
                },
                onConfirm = {
                    if (state.addressLookup != AddressLookupState.LOADING) onConfirm(viewModel.result())
                }
            )
        }
    }
    if (showAddressEditor) {
        AddressEditor(
            address = if (state.addressLookup in setOf(AddressLookupState.COORDINATES, AddressLookupState.ERROR, AddressLookupState.NOT_FOUND)) "" else state.address,
            city = state.city,
            onDismiss = { showAddressEditor = false },
            onSave = { address, city ->
                viewModel.onManualAddress(address, city)
                showAddressEditor = false
            }
        )
    }
}

private fun Location.coordinatesText() =
    "Lat: ${"%.4f".format(Locale.US, latitude)}, Lon: ${"%.4f".format(Locale.US, longitude)}"

@Composable
private fun SelectionCard(
    state: PickLocationUiState,
    onCopy: () -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onUseCoordinates: () -> Unit,
    onEditAddress: () -> Unit
) {
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
                Text(stringResource(R.string.pick_selected_point), color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(state.address, color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Text(state.city.ifBlank { stringResource(R.string.pick_city_unknown) }, color = MutedText, fontSize = 12.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(stringResource(R.string.pick_gps_coordinates), color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(state.selected.coordinatesText(), color = WarmText, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.pick_copy_coordinates), tint = Terracotta, modifier = Modifier.size(20.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.addressLookup == AddressLookupState.LOADING) {
                CircularProgressIndicator(Modifier.size(16.dp), color = Terracotta, strokeWidth = 2.dp)
            }
            Text(
                stringResource(when (state.addressLookup) {
                    AddressLookupState.LOADING -> R.string.pick_address_loading
                    AddressLookupState.FOUND -> R.string.pick_address_found
                    AddressLookupState.NOT_FOUND -> R.string.pick_address_not_found
                    AddressLookupState.ERROR -> R.string.pick_address_error
                    AddressLookupState.MANUAL -> R.string.pick_address_manual
                    AddressLookupState.COORDINATES -> R.string.pick_address_coordinates_only
                    AddressLookupState.SAVED -> R.string.pick_address_saved
                }),
                color = MutedText, fontSize = 11.sp, modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onEditAddress) { Text(stringResource(R.string.pick_edit_address), color = Terracotta) }
            if (state.addressLookup == AddressLookupState.LOADING) {
                TextButton(onClick = onUseCoordinates) { Text(stringResource(R.string.pick_use_coordinates), color = Terracotta) }
            } else {
                TextButton(onClick = onRetry) { Text(stringResource(R.string.pick_lookup_address), color = Terracotta) }
            }
        }
        PrimaryButton(
            text = stringResource(R.string.pick_confirm),
            onClick = onConfirm,
            enabled = state.addressLookup != AddressLookupState.LOADING,
            leadingIcon = Icons.Outlined.CheckCircle,
            containerColor = Terracotta
        )
    }
}

@Composable
private fun AddressEditor(address: String, city: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var editedAddress by rememberSaveable { mutableStateOf(address) }
    var editedCity by rememberSaveable { mutableStateOf(city) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_edit_address)) },
        text = {
            Column(
                modifier = Modifier.imePadding().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.pick_manual_address_help))
                val addressLabel = stringResource(R.string.pick_address_label)
                Text(addressLabel, color = WarmText, fontWeight = FontWeight.SemiBold)
                FormTextField(
                    value = editedAddress, onValueChange = { editedAddress = it },
                    placeholder = addressLabel, leadingIcon = Icons.Outlined.LocationOn,
                    modifier = Modifier.semantics { contentDescription = addressLabel }
                )
                val cityLabel = stringResource(R.string.pick_city_label)
                Text(cityLabel, color = WarmText, fontWeight = FontWeight.SemiBold)
                FormTextField(
                    value = editedCity, onValueChange = { editedCity = it },
                    placeholder = cityLabel, leadingIcon = Icons.Outlined.LocationCity,
                    modifier = Modifier.semantics { contentDescription = cityLabel }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editedAddress, editedCity) }, enabled = editedAddress.isNotBlank() && editedCity.isNotBlank()) {
                Text(stringResource(R.string.pick_save_address))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.pick_cancel_address)) } }
    )
}
