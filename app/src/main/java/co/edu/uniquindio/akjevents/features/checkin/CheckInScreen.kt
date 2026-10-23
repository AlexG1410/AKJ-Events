package co.edu.uniquindio.akjevents.features.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.component.ContentCard
import co.edu.uniquindio.akjevents.core.component.FormFieldStyle
import co.edu.uniquindio.akjevents.core.component.FormTextField
import co.edu.uniquindio.akjevents.core.component.PrimaryButton
import co.edu.uniquindio.akjevents.core.component.QrCode
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.Tag
import co.edu.uniquindio.akjevents.core.component.UserAvatar
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.Honey
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import co.edu.uniquindio.akjevents.domain.repository.CheckInResult

private val CodeFieldStyle = FormFieldStyle(
    height = 52.dp,
    shape = RoundedCornerShape(14.dp),
    containerColor = Color.White,
    borderColor = SurfaceContainer,
    focusedBorderColor = Terracotta,
    textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = WarmText, letterSpacing = 2.sp),
    placeholderColor = MutedText.copy(alpha = 0.6f),
    iconColor = Terracotta,
    iconSize = 20.dp,
    iconStartPadding = 14.dp,
    textStartPadding = 44.dp
)

@Composable
fun CheckInScreen(
    eventId: String?,
    showMyQr: Boolean,
    onBack: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: CheckInViewModel = viewModel { CheckInViewModel(eventId, showMyQr) }
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val user = state.user

    Scaffold(containerColor = WarmBackground, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        if (user == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(title = "Check-in de Asistencia", subtitle = "Lector de QR simulado", onBack = onBack)

            TabSwitcher(selected = state.tab, onSelect = viewModel::selectTab)

            when (state.tab) {
                CheckInTab.MY_QR -> MyQrCard(user)
                CheckInTab.SCAN -> {
                    val result = state.result
                    if (result is CheckInResult.Success) {
                        SuccessCard(
                            result = result,
                            user = user,
                            pointsBefore = state.pointsBefore,
                            organizer = state.organizer,
                            onOpenEvent = { onOpenEvent(result.event.id) },
                            onOpenProfile = onOpenProfile,
                            onScanAgain = viewModel::reset
                        )
                    } else {
                        ScannerBox()
                        FormTextField(
                            value = state.code,
                            onValueChange = viewModel::onCodeChange,
                            placeholder = "AKJ-XXXXXX",
                            leadingIcon = Icons.Outlined.Password,
                            style = CodeFieldStyle,
                            isError = state.errorMessage != null,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.validate()
                            })
                        )
                        state.errorMessage?.let { Text(it, color = ErrorColor, fontSize = 13.sp) }
                        PrimaryButton(
                            text = "Validar código",
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.validate()
                            },
                            leadingIcon = Icons.Outlined.QrCodeScanner,
                            containerColor = Terracotta
                        )
                        PendingEvents(events = state.pendingEvents, onScan = viewModel::scanEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabSwitcher(selected: CheckInTab, onSelect: (CheckInTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceContainer, CircleShape).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabOption("Escanear QR", Icons.Outlined.QrCodeScanner, selected == CheckInTab.SCAN, { onSelect(CheckInTab.SCAN) }, Modifier.weight(1f))
        TabOption("Mostrar mi QR", Icons.Outlined.QrCode2, selected == CheckInTab.MY_QR, { onSelect(CheckInTab.MY_QR) }, Modifier.weight(1f))
    }
}

@Composable
private fun TabOption(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick = onClick, shape = CircleShape, color = if (isSelected) Color.White else Color.Transparent, shadowElevation = if (isSelected) 1.dp else 0.dp, modifier = modifier) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Terracotta else MutedText, modifier = Modifier.size(18.dp))
            Text("  $text", color = if (isSelected) Terracotta else MutedText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Visor de "cámara" del mockup; el código se escribe o se elige abajo. */
@Composable
private fun ScannerBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF3A332E), Color(0xFF1E1B18))), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(130.dp).border(3.dp, Sage.copy(alpha = 0.8f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.QrCode2, contentDescription = null, tint = Honey, modifier = Modifier.size(48.dp))
                Text("Esperando código", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            "Escribe el código que muestra el QR del organizador",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)
        )
    }
}

@Composable
private fun PendingEvents(events: List<CommunityEvent>, onScan: (CommunityEvent) -> Unit) {
    ContentCard(spacing = 10.dp) {
        Text("Tus eventos confirmados", color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (events.isEmpty()) {
            Text(
                "Confirma tu asistencia a un evento para hacer check-in al llegar. También puedes escribir el código del organizador.",
                color = MutedText,
                fontSize = 13.sp
            )
        }
        events.forEach { event ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).background(PrimaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = Terracotta, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(EventFormat.shortDateTime(event.startsAt), color = MutedText, fontSize = 12.sp)
                }
                Surface(onClick = { onScan(event) }, shape = CircleShape, color = Sage) {
                    Text("Escanear", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SuccessCard(
    result: CheckInResult.Success,
    user: User,
    pointsBefore: Int,
    organizer: User?,
    onOpenEvent: () -> Unit,
    onOpenProfile: () -> Unit,
    onScanAgain: () -> Unit
) {
    val event = result.event
    val leveledUp = UserLevel.forPoints(pointsBefore) != user.level
    val next = user.level.next
    ContentCard(spacing = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(56.dp).background(SecondaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(32.dp))
            }
            Column {
                Tag("¡Asistencia confirmada!", container = SecondaryFixed, content = OnSecondaryFixedVariant, icon = Icons.Outlined.Verified)
                Text(event.title, color = WarmText, fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(14.dp))
                    Text(" ${event.address.substringBefore(",")} • ${EventFormat.timeOfDay(event.startsAt)}", color = MutedText, fontSize = 12.sp)
                }
            }
        }

        organizer?.let {
            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserAvatar(it, size = 36.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Anfitrión verificado", color = MutedText, fontSize = 11.sp)
                    Text(it.name, color = WarmText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).background(TertiaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = OnTertiaryContainer)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("RECOMPENSA CÍVICA", color = OnTertiaryContainer, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("+${result.pointsEarned} pts obtenidos", color = WarmText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Tag("Nivel ${user.level.number}", container = TertiaryFixed, content = OnTertiaryContainer)
            }
            Text(
                if (next == null) "Balance cívico: ${user.points} pts (nivel máximo)" else "Balance cívico: ${user.points} / ${next.minPoints} pts",
                color = MutedText,
                fontSize = 12.sp
            )
            CapacityBar(
                progress = if (next == null) 1f else (user.points - user.level.minPoints).toFloat() / (next.minPoints - user.level.minPoints),
                color = Sage
            )
            if (leveledUp) {
                Text("¡Felicidades! Alcanzaste el rango de ${user.level.label}", color = Sage, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Favorite, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
                Text(
                    "Tu presencia fortalece el tejido social de la comunidad.",
                    color = MutedText,
                    fontSize = 12.sp
                )
            }
        }

        PrimaryButton(text = "Ver detalles del evento", onClick = onOpenEvent, containerColor = Terracotta)
        Surface(onClick = onOpenProfile, shape = RoundedCornerShape(16.dp), color = PrimaryFixed, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = Terracotta, modifier = Modifier.size(18.dp))
                Text("  Ir a mi perfil y logros", color = Terracotta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Surface(onClick = onScanAgain, shape = RoundedCornerShape(16.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = MutedText, modifier = Modifier.size(16.dp))
                Text("  Escanear otro código", color = MutedText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MyQrCard(user: User) {
    ContentCard(spacing = 14.dp) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            UserAvatar(user, size = 56.dp)
            Text(user.name, color = WarmText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Tag("${user.level.label} • ${user.points} pts", container = TertiaryFixed, content = OnTertiaryContainer)
            Box(Modifier.background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, SurfaceContainer, RoundedCornerShape(16.dp)).padding(16.dp)) {
                QrCode(content = "AKJ-USER:${user.id}", modifier = Modifier.size(220.dp))
            }
            Text(
                "Tu código personal te identifica ante los organizadores en los eventos comunitarios.",
                color = MutedText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
