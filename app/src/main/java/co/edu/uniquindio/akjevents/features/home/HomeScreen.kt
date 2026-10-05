package co.edu.uniquindio.akjevents.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.AppBottomBar
import co.edu.uniquindio.akjevents.core.component.BottomDestination
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.component.CategoryChip
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.VerifiedBadge
import co.edu.uniquindio.akjevents.core.component.style
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.Honey
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.core.util.isFull
import co.edu.uniquindio.akjevents.core.util.occupancy
import co.edu.uniquindio.akjevents.core.util.remainingSpots
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

private const val COMING_SOON = "Disponible próximamente"

private val SearchFieldStyle = FormFieldStyle(
    height = 48.dp,
    shape = RoundedCornerShape(12.dp),
    containerColor = SurfaceContainerLow,
    focusedContainerColor = Color.White,
    borderColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    textStyle = TextStyle(fontSize = 14.sp, letterSpacing = 0.25.sp, color = WarmText),
    placeholderColor = MutedText.copy(alpha = 0.7f),
    iconColor = MutedText,
    iconSize = 20.dp,
    iconStartPadding = 12.dp,
    textStartPadding = 40.dp
)

@Composable
fun HomeScreen(
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val searchFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val showComingSoon: () -> Unit = {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(COMING_SOON)
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = { HomeTopBar(onSearchClick = { searchFocus.requestFocus() }) },
        bottomBar = {
            AppBottomBar(
                selected = BottomDestination.HOME,
                hasUnreadAlerts = true,
                onSelect = { if (it != BottomDestination.HOME) showComingSoon() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Greeting(
                    firstName = state.userFirstName,
                    city = state.city,
                    onCalendarClick = showComingSoon,
                    onChangeCity = showComingSoon
                )
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "Buscar eventos, talleres, deportes...",
                        leadingIcon = Icons.Outlined.Search,
                        style = SearchFieldStyle,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        trailingContent = if (state.query.isNotEmpty()) {
                            {
                                IconButton(onClick = { viewModel.onQueryChange("") }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Borrar búsqueda", tint = MutedText, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .focusRequester(searchFocus)
                    )
                    Surface(
                        onClick = showComingSoon,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Tune, contentDescription = "Filtros avanzados", tint = WarmText, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            item {
                CategoryChips(selected = state.category, onSelect = viewModel::selectCategory)
            }

            if (!state.hasResults) {
                item { EmptyResults() }
            }

            if (state.featuredEvents.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            icon = Icons.Outlined.LocalFireDepartment,
                            iconTint = Terracotta,
                            title = "Eventos Destacados",
                            action = "Ver mapa",
                            onAction = showComingSoon
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.featuredEvents, key = { it.id }) { event ->
                                FeaturedEventCard(
                                    event = event,
                                    isInterested = event.id in state.interestedEventIds,
                                    onOpen = { onOpenEvent(event.id) },
                                    onToggleInterest = { viewModel.toggleInterest(event.id) },
                                    modifier = Modifier.fillParentMaxWidth(0.86f).widthIn(max = 340.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (state.upcomingEvents.isNotEmpty()) {
                item {
                    SectionHeader(
                        icon = Icons.Outlined.Groups,
                        iconTint = Sage,
                        title = "Próximos en tu comunidad",
                        action = "Filtrar",
                        onAction = showComingSoon
                    )
                }
                items(state.upcomingEvents, key = { it.id }) { event ->
                    UpcomingEventCard(
                        event = event,
                        isInterested = event.id in state.interestedEventIds,
                        onOpen = { onOpenEvent(event.id) },
                        onToggleInterest = { viewModel.toggleInterest(event.id) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item { LeaderBanner(onCreate = showComingSoon) }
        }
    }
}

@Composable
private fun HomeTopBar(onSearchClick: () -> Unit) {
    Surface(color = WarmBackground.copy(alpha = 0.95f), shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo AKJ Events",
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("AKJ EVENTS", color = Terracotta, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Inicio", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = MutedText)
            }
            Image(
                painter = painterResource(R.drawable.avatar),
                contentDescription = "Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(32.dp).shadow(1.dp, CircleShape).clip(CircleShape)
            )
        }
    }
}

@Composable
private fun Greeting(firstName: String, city: String, onCalendarClick: () -> Unit, onChangeCity: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("COMUNIDAD QUINDÍO", color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("¡Hola, $firstName! 👋", color = WarmText, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold)
            }
            Surface(onClick = onCalendarClick, modifier = Modifier.size(44.dp), shape = CircleShape, color = SurfaceContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Today, contentDescription = "Mi agenda", tint = MutedText, modifier = Modifier.size(22.dp))
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .size(8.dp)
                            .background(Terracotta, CircleShape)
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceContainer)
                .clickable(onClick = onChangeCity)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
            Text(city, color = WarmText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "Cambiar",
                color = Terracotta,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
private fun CategoryChips(selected: EventCategory?, onSelect: (EventCategory?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterPill(text = "Todos", icon = Icons.Outlined.Stars, isSelected = selected == null, onClick = { onSelect(null) })
        }
        items(EventCategory.entries) { category ->
            FilterPill(
                text = "${category.label} ${category.style.emoji}",
                isSelected = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun FilterPill(text: String, isSelected: Boolean, onClick: () -> Unit, icon: ImageVector? = null) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) Terracotta else SurfaceContainer,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon?.let {
                Icon(it, contentDescription = null, tint = if (isSelected) Color.White else WarmText, modifier = Modifier.size(16.dp))
            }
            Text(
                text,
                color = if (isSelected) Color.White else WarmText,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, iconTint: Color, title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Text(title, color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(
            action,
            color = Terracotta,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onAction).padding(4.dp)
        )
    }
}

@Composable
private fun EventImage(event: CommunityEvent, modifier: Modifier = Modifier) {
    AsyncImage(
        model = event.imageUrls.first(),
        contentDescription = "Imagen del evento ${event.title}",
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.event_placeholder),
        error = painterResource(R.drawable.event_placeholder),
        modifier = modifier
    )
}

@Composable
private fun FeaturedEventCard(
    event: CommunityEvent,
    isInterested: Boolean,
    onOpen: () -> Unit,
    onToggleInterest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(onClick = onOpen, modifier = modifier, shape = shape, color = Color.White, shadowElevation = 3.dp) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
                EventImage(event, Modifier.fillMaxSize())
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                )
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(48.dp)
                        .shadow(1.dp, RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(EventFormat.monthBadge(event.startsAt), color = Terracotta, fontSize = 11.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
                    Text(event.startsAt.dayOfMonth.toString(), color = WarmText, fontSize = 16.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
                }
                CategoryChip(event.category, showIcon = true, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp))
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = TertiaryFixed, modifier = Modifier.size(16.dp))
                    Text(EventFormat.weekdayDateTime(event.startsAt), color = Color.White, fontSize = 12.sp)
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            event.title,
                            color = WarmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        VerifiedBadge()
                    }
                    PlaceRow(event.address, Icons.Outlined.PinDrop)
                }

                CapacitySummary(event)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Surface(
                        onClick = onOpen,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = shape,
                        color = Terracotta,
                        shadowElevation = 2.dp
                    ) {
                        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (event.isFull) "Ver detalle" else "Inscribirse gratis",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.size(8.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        onClick = onToggleInterest,
                        modifier = Modifier.size(44.dp),
                        shape = shape,
                        color = if (isInterested) PrimaryFixed else SurfaceContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isInterested) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isInterested) "Quitar de me interesa" else "Me interesa",
                                tint = if (isInterested) Terracotta else MutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceRow(address: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
        Text(
            // Sin el departamento, como en el mockup ("Plaza de Bolívar, Armenia")
            address.removeSuffix(", Quindío"),
            color = MutedText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CapacitySummary(event: CommunityEvent) {
    val remaining = event.remainingSpots
    val occupancy = event.occupancy
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${event.attendanceCount} confirmados", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(
                when (remaining) {
                    null -> "Cupo abierto"
                    0 -> "Cupo lleno"
                    1 -> "1 cupo libre"
                    else -> "$remaining cupos libres"
                },
                color = if (remaining == null) Sage else Terracotta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Sin cupo máximo la barra no aplica
        if (occupancy != null) {
            CapacityBar(progress = occupancy, color = if (occupancy >= 0.9f) Honey else Terracotta)
        }
    }
}

@Composable
private fun UpcomingEventCard(
    event: CommunityEvent,
    isInterested: Boolean,
    onOpen: () -> Unit,
    onToggleInterest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(onClick = onOpen, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))) {
                    EventImage(event, Modifier.fillMaxSize())
                    Text(
                        "${event.startsAt.dayOfMonth} ${EventFormat.monthBadge(event.startsAt)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Column(modifier = Modifier.weight(1f).height(96.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryChip(event.category)
                            VerifiedBadge(iconSize = 12.dp)
                        }
                        Text(
                            event.title,
                            color = WarmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(EventFormat.weekdayDateTime(event.startsAt), color = MutedText, fontSize = 12.sp, maxLines = 1)
                    }
                    PlaceRow(event.address, Icons.Outlined.LocationOn)
                }
            }
            HorizontalDivider(color = SurfaceContainer, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvailabilityLabel(event)
                Text("  •  ", color = MutedText.copy(alpha = 0.4f), fontSize = 11.sp)
                Text(
                    "${event.attendanceCount} asistirán",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                InterestPill(isInterested = isInterested, onClick = onToggleInterest)
            }
        }
    }
}

@Composable
private fun AvailabilityLabel(event: CommunityEvent) {
    val remaining = event.remainingSpots
    val (text, color) = when {
        remaining == null -> "Cupo abierto" to Sage
        remaining == 0 -> "Cupo lleno" to ErrorColor
        remaining <= 5 -> "¡Últimos $remaining cupos!" to ErrorColor
        else -> "$remaining cupos libres" to Terracotta
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun InterestPill(isInterested: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = if (isInterested) Terracotta else PrimaryFixed) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val color = if (isInterested) Color.White else OnPrimaryFixedVariant
            Icon(if (isInterested) Icons.Rounded.Star else Icons.Outlined.StarOutline, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text("Me interesa", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LeaderBanner(onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .background(SurfaceContainer, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("¿ERES LÍDER O GESTOR?", color = Terracotta, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Crea y publica tu propio evento comunitario", color = WarmText, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
            Text("Conecta con cientos de personas activas en el Quindío.", color = MutedText, fontSize = 12.sp, lineHeight = 16.sp)
            Surface(
                onClick = onCreate,
                shape = RoundedCornerShape(12.dp),
                color = Terracotta,
                shadowElevation = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Crear iniciativa", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Box(
            modifier = Modifier.size(64.dp).background(PrimaryFixed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Campaign, contentDescription = null, tint = Terracotta, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun EmptyResults() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = MutedText, modifier = Modifier.size(40.dp))
        Text("No encontramos eventos", color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(
            "Prueba con otra búsqueda o categoría.",
            color = MutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}
