package co.edu.uniquindio.akjevents.features.map.explore

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.component.CategoryChip
import co.edu.uniquindio.akjevents.core.component.EmptyState
import co.edu.uniquindio.akjevents.core.component.EventImage
import co.edu.uniquindio.akjevents.core.component.FilterPill
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.MainTopBar
import co.edu.uniquindio.akjevents.core.component.MapMarker
import co.edu.uniquindio.akjevents.core.component.SimulatedMap
import co.edu.uniquindio.akjevents.core.component.VerifiedBadge
import co.edu.uniquindio.akjevents.core.component.rememberSimulatedMapState
import co.edu.uniquindio.akjevents.core.component.style
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.SecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.core.util.labelRes
import co.edu.uniquindio.akjevents.core.util.occupancy
import co.edu.uniquindio.akjevents.core.util.remainingSpots
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import java.time.LocalDate

private val MapSearchStyle = FormFieldStyle(
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

@Composable
fun MapScreen(
    onOpenEvent: (String) -> Unit,
    onOpenList: () -> Unit,
    onOpenProfile: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val userLocation = state.user?.location ?: Location(4.5339, -75.6811)
    val mapState = rememberSimulatedMapState(center = userLocation, latSpan = 0.03)
    var showCategories by remember { mutableStateOf(false) }

    // Al elegir otro evento, el mapa lo centra
    LaunchedEffect(state.selectedEventId) {
        state.selectedEvent?.let { mapState.moveTo(it.location) }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = { MainTopBar(title = stringResource(R.string.nav_map), user = state.user, onAvatarClick = onOpenProfile) },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.map_search_placeholder),
                leadingIcon = Icons.Outlined.Search,
                style = MapSearchStyle,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
                item {
                    FilterPill(
                        text = stringResource(R.string.location_filter_nearby, MapViewModel.NEARBY_RADIUS_KM),
                        icon = Icons.Outlined.NearMe,
                        isSelected = state.nearbyOnly,
                        onClick = {
                            viewModel.toggleNearby()
                            mapState.moveTo(userLocation)
                        }
                    )
                }
                item {
                    Box {
                        FilterPill(
                            text = state.category?.let { stringResource(it.labelRes) } ?: stringResource(R.string.map_layers),
                            icon = Icons.Outlined.Layers,
                            isSelected = state.category != null,
                            onClick = { showCategories = true }
                        )
                        DropdownMenu(expanded = showCategories, onDismissRequest = { showCategories = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.map_all_categories)) }, onClick = {
                                viewModel.selectCategory(null)
                                showCategories = false
                            })
                            EventCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(category.labelRes)) },
                                    leadingIcon = { Icon(category.style.icon, contentDescription = null) },
                                    onClick = {
                                        viewModel.selectCategory(category)
                                        showCategories = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    FilterPill(
                        text = stringResource(R.string.map_this_week),
                        icon = Icons.Outlined.CalendarToday,
                        isSelected = state.thisWeekOnly,
                        onClick = viewModel::toggleThisWeek
                    )
                }
            }

            SimulatedMap(
                state = mapState,
                markers = state.events.map { event ->
                    val style = event.category.style
                    MapMarker(event.id, event.location, event.title.shortLabel(), style.icon, style.containerColor, style.contentColor)
                },
                selectedMarkerId = state.selectedEventId,
                onMarkerClick = viewModel::selectEvent,
                userLocation = userLocation,
                modifier = Modifier.fillMaxWidth().height(380.dp).padding(horizontal = 16.dp)
            ) {
                Surface(
                    onClick = onOpenList,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.List, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.map_see_list), color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            val selected = state.selectedEvent
            if (selected != null) {
                val directionsTitle = stringResource(R.string.map_directions)
                SelectedEventCard(
                    event = selected,
                    distanceKm = state.distancesKm[selected.id],
                    onOpen = { onOpenEvent(selected.id) },
                    onDirections = {
                        val uri = Uri.parse("geo:${selected.location.latitude},${selected.location.longitude}?q=${selected.location.latitude},${selected.location.longitude}(${Uri.encode(selected.title)})")
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), directionsTitle))
                    }
                )
            } else {
                EmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.map_empty_title),
                    message = stringResource(R.string.map_empty_message),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

/** Las primeras palabras del título, para que el marcador no tape el mapa. */
private fun String.shortLabel(): String {
    val words = split(" ")
    return if (words.size <= 2) this else words.take(2).joinToString(" ")
}

@Composable
private fun SelectedEventCard(event: CommunityEvent, distanceKm: Double?, onOpen: () -> Unit, onDirections: () -> Unit) {
    val isToday = event.startsAt.toLocalDate() == LocalDate.now()
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(84.dp).clip(RoundedCornerShape(12.dp))) {
                EventImage(event, Modifier.fillMaxSize())
                if (isToday) {
                    Text(
                        stringResource(R.string.map_today),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(6.dp).background(Terracotta, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    VerifiedBadge()
                    CategoryChip(event.category)
                }
                Text(event.title, color = WarmText, fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(event.address.removeSuffix(", Quindío"), color = MutedText, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoItem(Icons.Outlined.Schedule, PrimaryFixed, Terracotta, stringResource(R.string.map_schedule), EventFormat.shortDateTime(event.startsAt), Modifier.weight(1f))
            InfoItem(
                Icons.AutoMirrored.Outlined.DirectionsWalk,
                SecondaryContainer,
                OnSecondaryContainer,
                stringResource(R.string.map_distance),
                distanceKm?.let { stringResource(R.string.map_distance_value, EventFormat.distance(it)) } ?: "—",
                Modifier.weight(1f)
            )
        }
        val remaining = event.remainingSpots
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.Groups, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
            Text(pluralStringResource(R.plurals.attendees_confirmed_long, event.attendanceCount, event.attendanceCount), color = WarmText, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(
                when (remaining) {
                    null -> stringResource(R.string.open_capacity)
                    0 -> stringResource(R.string.capacity_full)
                    else -> pluralStringResource(R.plurals.spots_remaining, remaining, remaining)
                },
                color = Terracotta,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        event.occupancy?.let { CapacityBar(progress = it, color = Terracotta) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(onClick = onOpen, shape = RoundedCornerShape(12.dp), color = Terracotta, modifier = Modifier.weight(1f).height(48.dp)) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.map_see_event), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.padding(start = 8.dp).size(18.dp))
                }
            }
            Surface(onClick = onDirections, shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh, modifier = Modifier.weight(1f).height(48.dp)) {
                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Explore, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(18.dp))
                    Text("  " + stringResource(R.string.map_directions), color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    container: Color,
    tint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(36.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, color = MutedText, fontSize = 11.sp)
            Text(value, color = WarmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
