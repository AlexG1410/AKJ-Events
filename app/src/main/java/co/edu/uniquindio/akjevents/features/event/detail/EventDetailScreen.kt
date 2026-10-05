package co.edu.uniquindio.akjevents.features.event.detail

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.LaunchedEffect
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
    viewModel: EventDetailViewModel = viewModel(key = eventId) { EventDetailViewModel(eventId) }
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(state.userMessage) {
        val message = state.userMessage ?: return@LaunchedEffect
        // Se muestra en otro scope para que limpiar el mensaje no cancele el Snackbar
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
        viewModel.onMessageShown()
    }

    val showComingSoon: () -> Unit = {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar("Disponible próximamente")
        }
    }

    val event = state.event
    Scaffold(
        containerColor = WarmBackground,
        topBar = { DetailTopBar(onBack = onBack) },
        bottomBar = {
            if (event != null) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            QuickActions(
                event = event,
                isInterested = state.isInterested,
                onShare = { context.startActivity(shareIntent(event)) },
                onToggleInterest = viewModel::toggleInterest
            )
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
            CapacityCard(event)
            DescriptionSection(event.description)
            state.organizer?.let { organizer ->
                OrganizerSection(organizer, state.organizerEventCount, onContact = showComingSoon)
            }
        }
    }
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
private fun DetailTopBar(onBack: () -> Unit) {
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
            Text("Detalle del evento", color = WarmText, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuickActions(event: CommunityEvent, isInterested: Boolean, onShare: () -> Unit, onToggleInterest: () -> Unit) {
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
        RoundAction(
            if (isInterested) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
            if (isInterested) "Quitar de me interesa" else "Me interesa",
            Terracotta,
            onToggleInterest
        )
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
            HeroBadge(icon = Icons.Rounded.Verified, text = "Verificado ✓", container = Sage, content = Color.White, iconTint = Color.White)
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
private fun OrganizerSection(organizer: User, eventCount: Int, onContact: () -> Unit) {
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
                    Text(organizer.initials(), color = OnPrimaryFixed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
            Surface(onClick = onContact, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = SurfaceContainerHigh) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = WarmText, modifier = Modifier.size(18.dp))
                    Text("  Contactar organizador", color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun User.initials(): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

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
