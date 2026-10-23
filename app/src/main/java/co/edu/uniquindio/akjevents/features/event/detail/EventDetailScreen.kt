package co.edu.uniquindio.akjevents.features.event.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.MapMarker
import co.edu.uniquindio.akjevents.core.component.MessageEffect
import co.edu.uniquindio.akjevents.core.component.SimulatedMap
import co.edu.uniquindio.akjevents.core.component.StatusBadge
import co.edu.uniquindio.akjevents.core.component.UserAvatar
import co.edu.uniquindio.akjevents.core.component.rememberSimulatedMapState
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.component.style
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.core.util.occupancy
import co.edu.uniquindio.akjevents.core.util.remainingSpots
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    onEditEvent: (String) -> Unit,
    onOpenCheckIn: (String) -> Unit,
    viewModel: EventDetailViewModel = viewModel(key = eventId) { EventDetailViewModel(eventId) }
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val commentFocus = remember { FocusRequester() }

    MessageEffect(state.userMessage, snackbarHostState, viewModel::onMessageShown)

    val event = state.event
    Scaffold(
        containerColor = WarmBackground,
        topBar = {
            DetailTopBar(
                onBack = onBack,
                onEdit = if (state.canEdit && event != null) {
                    { onEditEvent(event.id) }
                } else null
            )
        },
        bottomBar = {
            if (event != null && state.canParticipate) {
                AttendanceBar(
                    isInterested = state.isInterested,
                    isAttending = state.isAttending,
                    onToggleInterest = viewModel::toggleInterest,
                    onToggleAttendance = viewModel::toggleAttendance
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (event == null) {
            UnavailableEvent(onBack = onBack, modifier = Modifier.padding(innerPadding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            QuickActions(
                event = event,
                isInterested = state.isInterested,
                canToggleInterest = state.canParticipate,
                onShare = { context.startActivity(shareIntent(event)) },
                onToggleInterest = viewModel::toggleInterest
            )
            if (event.status != EventStatus.VERIFIED) {
                StatusBanner(event = event, isOwner = state.isOwner)
            }
            HeroImage(event)
            Text(
                event.title,
                color = WarmText,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            DateAndPlaceCard(event)
            if (state.isAttending && state.canParticipate) {
                CheckInCard(isCheckedIn = state.isCheckedIn, onOpenCheckIn = { onOpenCheckIn(event.id) })
            }
            CapacityCard(event)
            DescriptionSection(event.description)
            LocationSection(
                event = event,
                distanceKm = state.distanceKm,
                onDirections = { context.startActivity(directionsIntent(event)) }
            )
            state.organizer?.let { organizer ->
                OrganizerSection(
                    organizer = organizer,
                    eventCount = state.organizerEventCount,
                    showContact = state.canComment && !state.isOwner,
                    onContact = {
                        scope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                            commentFocus.requestFocus()
                        }
                    }
                )
            }
            CommentsSection(
                comments = state.comments,
                organizerId = event.organizerId,
                canComment = state.canComment,
                draft = state.commentDraft,
                onDraftChange = viewModel::onCommentChange,
                onSend = viewModel::sendComment,
                focusRequester = commentFocus
            )
        }
    }
}

private fun directionsIntent(event: CommunityEvent): Intent {
    // Abre la app de mapas del teléfono en las coordenadas del evento
    val uri = Uri.parse("geo:${event.location.latitude},${event.location.longitude}?q=${event.location.latitude},${event.location.longitude}(${Uri.encode(event.title)})")
    return Intent.createChooser(Intent(Intent.ACTION_VIEW, uri), "Cómo llegar")
}

private fun shareIntent(event: CommunityEvent): Intent {
    val text = "${event.title}\n${EventFormat.weekdayDateTime(event.startsAt)}\n${event.address}\n\nDescúbrelo en AKJ Events."
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, "Compartir evento")
}

@Composable
private fun DetailTopBar(onBack: () -> Unit, onEdit: (() -> Unit)?) {
    Surface(color = WarmBackground.copy(alpha = 0.95f), shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = WarmText)
            }
            Image(painter = painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(32.dp))
            Text(
                "Detalle del evento",
                color = WarmText,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Editar evento", tint = Terracotta)
                }
            }
        }
    }
}

@Composable
private fun QuickActions(
    event: CommunityEvent,
    isInterested: Boolean,
    canToggleInterest: Boolean,
    onShare: () -> Unit,
    onToggleInterest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(SecondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(event.category.style.icon, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(18.dp))
        }
        Text(
            "EVENTO COMUNITARIO",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )
        RoundAction(Icons.Outlined.Share, "Compartir evento", WarmText, onShare)
        if (canToggleInterest) {
            RoundAction(
                if (isInterested) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                if (isInterested) "Quitar de me interesa" else "Me interesa",
                Terracotta,
                onToggleInterest
            )
        }
    }
}

@Composable
private fun RoundAction(icon: ImageVector, description: String, tint: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.size(40.dp), shape = CircleShape, color = SurfaceContainer, shadowElevation = 1.dp) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun HeroImage(event: CommunityEvent) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .shadow(4.dp, CardShape)
            .clip(CardShape)
            .background(SurfaceContainerHigh)
    ) {
        AsyncImage(
            model = event.imageUrls.first(),
            contentDescription = "Imagen del evento ${event.title}",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.event_placeholder),
            error = painterResource(R.drawable.event_placeholder),
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.7f))))
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeroBadge(
                icon = event.category.style.icon,
                text = event.category.label,
                container = WarmBackground.copy(alpha = 0.9f),
                content = WarmText,
                iconTint = Terracotta
            )
            if (event.status == EventStatus.VERIFIED) {
                HeroBadge(icon = Icons.Rounded.Verified, text = "Verificado ✓", container = Sage, content = Color.White, iconTint = Color.White)
            } else {
                StatusBadge(event.status)
            }
        }
    }
}

@Composable
private fun HeroBadge(icon: ImageVector, text: String, container: Color, content: Color, iconTint: Color) {
    Row(
        modifier = Modifier
            .shadow(1.dp, CircleShape)
            .background(container, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Text(text, color = content, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DateAndPlaceCard(event: CommunityEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .background(SurfaceContainer, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier.size(40.dp).background(PrimaryFixed, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(EventFormat.monthBadge(event.startsAt), color = OnPrimaryFixed, fontSize = 10.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
                Text(event.startsAt.dayOfMonth.toString(), color = OnPrimaryFixed, fontSize = 14.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
            }
            Column {
                Text(EventFormat.longDate(event.startsAt), color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Text(EventFormat.timeRange(event.startsAt, event.endsAt), color = MutedText, fontSize = 12.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(SecondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(20.dp))
            }
            Text(event.address, color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CapacityCard(event: CommunityEvent) {
    val remaining = event.remainingSpots
    val occupancy = event.occupancy
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape)
            .background(SurfaceContainerLow, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cupos y comunidad", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    capacityMessage(remaining, event.capacity),
                    color = Terracotta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (occupancy != null) {
                Text(
                    "${(occupancy * 100).roundToInt()}% lleno",
                    color = OnPrimaryFixed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(PrimaryFixed, CircleShape).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        if (occupancy != null) {
            CapacityBar(progress = occupancy, color = Terracotta, height = 10.dp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = WarmText, fontWeight = FontWeight.SemiBold)) {
                        append(if (event.attendanceCount == 1) "1 persona" else "${event.attendanceCount} personas")
                    }
                    append(" ya confirmadas · ${event.interestCount} interesadas")
                },
                color = MutedText,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Outlined.Group, contentDescription = null, tint = Sage, modifier = Modifier.size(20.dp))
        }
    }
}

private fun capacityMessage(remaining: Int?, capacity: Int?): String = when {
    remaining == null || capacity == null -> "Cupo abierto, sin límite de asistentes"
    remaining == 0 -> "Cupo lleno"
    remaining == 1 -> "¡Último cupo disponible!"
    remaining <= capacity / 5 -> "¡Últimos $remaining cupos disponibles!"
    else -> "$remaining cupos disponibles de $capacity"
}

@Composable
private fun DescriptionSection(description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Detalles del evento", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            description,
            color = WarmText,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.25.sp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, CardShape)
                .background(Color.White, CardShape)
                .padding(16.dp)
        )
    }
}

@Composable
private fun OrganizerSection(organizer: User, eventCount: Int, showContact: Boolean, onContact: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Organizador", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (organizer.level == UserLevel.COMMUNITY_LEADER) {
                Text("Anfitrión destacado", color = Sage, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, CardShape)
                .background(SurfaceContainer, CardShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(56.dp).background(PrimaryFixed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(organizer.initials, color = OnPrimaryFixed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(organizer.name, color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Organizador verificado", tint = Sage, modifier = Modifier.size(18.dp))
                    }
                    Text("${organizer.level.label} 🏅", color = Terracotta, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${organizer.points} pts  •  ${if (eventCount == 1) "1 evento publicado" else "$eventCount eventos publicados"}",
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
            }
            if (showContact) {
                Surface(onClick = onContact, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = WarmText, modifier = Modifier.size(18.dp))
                        Text("  Preguntar al organizador", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Aviso para el organizador o el moderador cuando el evento no es público. */
private data class BannerContent(val title: String, val message: String, val container: Color, val content: Color)

@Composable
private fun StatusBanner(event: CommunityEvent, isOwner: Boolean) {
    val banner = when (event.status) {
        EventStatus.PENDING -> BannerContent(
            "En revisión de moderación",
            if (isOwner) "Solo tú y los moderadores pueden verlo. Será público cuando lo aprueben." else "Este evento espera la verificación de un moderador.",
            TertiaryFixed,
            OnTertiaryContainer
        )
        EventStatus.REJECTED -> BannerContent("Evento rechazado", "Motivo: ${event.rejectionReason}", Color(0xFFFFDAD6), ErrorColor)
        else -> BannerContent("Evento finalizado", "Este evento ya terminó; ya no recibe confirmaciones de asistencia.", SurfaceContainerHigh, MutedText)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(banner.container, CardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatusBadge(event.status)
        Text(banner.title, color = banner.content, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(banner.message, color = banner.content, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun CheckInCard(isCheckedIn: Boolean, onOpenCheckIn: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SecondaryFixed.copy(alpha = 0.6f), CardShape)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(Sage, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isCheckedIn) "Asistencia registrada con QR" else "Ya tienes tu cupo",
                color = OnSecondaryFixedVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (isCheckedIn) "Sumaste puntos de reputación por asistir." else "Al llegar, escanea el QR del organizador para registrar tu asistencia.",
                color = OnSecondaryFixedVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        if (!isCheckedIn) {
            Surface(onClick = onOpenCheckIn, shape = CircleShape, color = Sage) {
                Text(
                    "Check-in",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationSection(event: CommunityEvent, distanceKm: Double?, onDirections: () -> Unit) {
    val mapState = rememberSimulatedMapState(center = event.location, latSpan = 0.02)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ubicación exacta", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, CardShape)
                .background(Color.White, CardShape)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SimulatedMap(
                state = mapState,
                markers = listOf(MapMarker(event.id, event.location, event.category.label, event.category.style.icon, Terracotta)),
                selectedMarkerId = event.id,
                interactive = false,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.address, color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Lat ${"%.4f".format(Locale.US, event.location.latitude)}, Lon ${"%.4f".format(Locale.US, event.location.longitude)}" +
                            (distanceKm?.let { " • a ${EventFormat.distance(it)} de ti" } ?: ""),
                        color = MutedText,
                        fontSize = 12.sp
                    )
                }
                Surface(onClick = onDirections, shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Outlined.Directions, contentDescription = null, tint = Sage, modifier = Modifier.size(18.dp))
                        Text("Cómo llegar", color = WarmText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsSection(
    comments: List<CommentItem>,
    organizerId: String,
    canComment: Boolean,
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    focusRequester: FocusRequester
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Comentarios", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${comments.size}", color = Terracotta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        if (comments.isEmpty()) {
            Text(
                "Aún no hay comentarios. Comparte información útil o pregúntale al organizador.",
                color = MutedText,
                fontSize = 13.sp
            )
        }
        comments.forEach { item ->
            CommentRow(item, isOrganizer = item.comment.authorId == organizerId)
        }
        if (canComment) {
            FormTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = "Escribe un comentario...",
                leadingIcon = Icons.Outlined.ChatBubbleOutline,
                style = CommentFieldStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                trailingContent = {
                    IconButton(onClick = onSend, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Publicar comentario", tint = Terracotta, modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

private val CommentFieldStyle = FormFieldStyle(
    height = 52.dp,
    shape = RoundedCornerShape(16.dp),
    containerColor = Color.White,
    borderColor = SurfaceContainerHigh,
    focusedBorderColor = Terracotta,
    textStyle = TextStyle(fontSize = 14.sp, color = WarmText),
    placeholderColor = MutedText.copy(alpha = 0.7f),
    iconColor = MutedText,
    iconSize = 20.dp,
    iconStartPadding = 14.dp,
    textStartPadding = 44.dp
)

@Composable
private fun CommentRow(item: CommentItem, isOrganizer: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        if (item.author != null) {
            UserAvatar(item.author, size = 36.dp)
        } else {
            Box(Modifier.size(36.dp).background(SurfaceContainerHigh, CircleShape))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .background(if (isOrganizer) SecondaryFixed.copy(alpha = 0.5f) else SurfaceContainerLow, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    item.author?.name ?: "Cuenta eliminada",
                    color = WarmText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isOrganizer) {
                    Text("Organizador", color = Sage, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(item.comment.text, color = WarmText, fontSize = 14.sp, lineHeight = 20.sp)
            Text(EventFormat.relative(item.comment.createdAt), color = MutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AttendanceBar(
    isInterested: Boolean,
    isAttending: Boolean,
    onToggleInterest: () -> Unit,
    onToggleAttendance: () -> Unit
) {
    Surface(color = WarmBackground.copy(alpha = 0.95f), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = onToggleInterest,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isInterested) PrimaryFixed else SurfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(if (isInterested) Icons.Rounded.Star else Icons.Outlined.StarOutline, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                    Text(if (isInterested) "Te interesa" else "Me interesa", color = Terracotta, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            // Ya inscrito: el botón pasa a ser secundario y permite cancelar
            val contentColor = if (isAttending) Terracotta else Color.White
            Surface(
                onClick = onToggleAttendance,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isAttending) Color.White else Terracotta,
                border = if (isAttending) BorderStroke(1.5.dp, Terracotta) else null,
                shadowElevation = if (isAttending) 0.dp else 4.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isAttending) Icons.Outlined.EventBusy else Icons.Outlined.HowToReg,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (isAttending) "  Cancelar asistencia" else "  Confirmar asistencia",
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UnavailableEvent(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Icon(Icons.Outlined.EventBusy, contentDescription = null, tint = MutedText, modifier = Modifier.size(48.dp))
        Text("Este evento no está disponible", color = WarmText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Puede que aún esté en revisión o que ya no sea público.", color = MutedText, fontSize = 14.sp)
        Surface(onClick = onBack, shape = RoundedCornerShape(12.dp), color = Terracotta) {
            Text(
                "Volver al inicio",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
