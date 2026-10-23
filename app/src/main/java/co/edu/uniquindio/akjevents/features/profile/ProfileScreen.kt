package co.edu.uniquindio.akjevents.features.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.ConfirmDialog
import co.edu.uniquindio.akjevents.core.component.ContentCard
import co.edu.uniquindio.akjevents.core.component.MainTopBar
import co.edu.uniquindio.akjevents.core.component.MenuRow
import co.edu.uniquindio.akjevents.core.component.RoundIconButton
import co.edu.uniquindio.akjevents.core.component.SectionTitle
import co.edu.uniquindio.akjevents.core.component.StatusBadge
import co.edu.uniquindio.akjevents.core.component.Tag
import co.edu.uniquindio.akjevents.core.component.UserAvatar
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenMyEvents: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenMyQr: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenModeration: () -> Unit,
    onLoggedOut: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var confirmLogout by remember { mutableStateOf(false) }
    val user = state.user

    Scaffold(
        containerColor = WarmBackground,
        topBar = { MainTopBar(title = "Mi Perfil", user = user) },
        bottomBar = bottomBar
    ) { innerPadding ->
        if (user == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mi Perfil", color = WarmText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Tag("Activo", container = SecondaryFixed, content = OnSecondaryFixedVariant)
                Box(Modifier.weight(1f))
                RoundIconButton(Icons.Outlined.Share, "Compartir perfil", onClick = { context.startActivity(shareProfileIntent(user)) })
                RoundIconButton(Icons.Outlined.Settings, "Editar perfil", onClick = onEditProfile)
            }

            ProfileHeaderCard(user)

            if (user.isModerator) {
                ContentCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).background(SecondaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = OnSecondaryFixedVariant)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cuenta de moderación", color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Verificas o rechazas los eventos de la comunidad y puedes marcarlos como finalizados. " +
                                    "Los moderadores no acumulan puntos de reputación.",
                                color = MutedText,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                CommunityStats(state.stats)

                ContentCard {
                    SectionTitle(
                        "Insignias y Logros",
                        action = "${user.badges.size} de ${Badge.entries.size}",
                        onAction = onOpenStats
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Badge.entries.forEach { badge ->
                            BadgeItem(badge, unlocked = badge in user.badges, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Mis Eventos Creados", action = "Administrar", onAction = onOpenMyEvents)
                    if (state.recentEvents.isEmpty()) {
                        Text("Aún no has creado eventos. ¡Propón el primero desde el botón Crear!", color = MutedText, fontSize = 13.sp)
                    }
                    state.recentEvents.forEach { event -> RecentEventRow(event, onClick = { onOpenEvent(event.id) }) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Gestión y Privacidad")
                ContentCard(spacing = 0.dp, padding = 8.dp) {
                    MenuRow(Icons.Outlined.ManageAccounts, PrimaryFixed, Terracotta, "Editar perfil", "Datos personales, ubicación e intereses", onEditProfile)
                    if (user.isModerator) {
                        MenuRow(Icons.Outlined.Shield, SecondaryFixed, OnSecondaryFixedVariant, "Panel de moderación", "Eventos pendientes de revisión", onOpenModeration)
                    } else {
                        MenuRow(Icons.AutoMirrored.Outlined.ShowChart, SecondaryFixed, OnSecondaryFixedVariant, "Mis estadísticas", "Métricas, asistencias e historial de puntos", onOpenStats)
                        MenuRow(Icons.Outlined.QrCode2, SurfaceContainerHigh, WarmText, "Código QR personal de asistencia", "Check-in rápido en eventos comunitarios", onOpenMyQr)
                        MenuRow(Icons.Outlined.EventAvailable, TertiaryFixed, OnTertiaryContainer, "Mis eventos", "Pendientes, verificados, rechazados y finalizados", onOpenMyEvents)
                    }
                    MenuRow(Icons.Outlined.Notifications, SurfaceContainer, WarmText, "Notificaciones", "Comentarios, moderación y logros", onOpenNotifications)
                    MenuRow(
                        Icons.AutoMirrored.Outlined.Logout,
                        Color(0xFFFFDAD6),
                        ErrorColor,
                        "Cerrar sesión",
                        "Desconectar la cuenta en este dispositivo",
                        onClick = { confirmLogout = true },
                        titleColor = ErrorColor
                    )
                }
            }

            Text(
                "AKJ Events • Fase 2 • Comunidad del Quindío",
                color = MutedText,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "¿Cerrar sesión?",
            message = "Tendrás que ingresar de nuevo con tu correo y contraseña.",
            confirmText = "Cerrar sesión",
            isDestructive = true,
            onConfirm = {
                confirmLogout = false
                viewModel.logout()
                onLoggedOut()
            },
            onDismiss = { confirmLogout = false }
        )
    }
}

private fun shareProfileIntent(user: User): Intent {
    val text = "Soy ${user.name}, ${user.level.label} en AKJ Events con ${user.points} puntos cívicos. ¡Únete a los eventos de la comunidad!"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, "Compartir perfil")
}

@Composable
private fun ProfileHeaderCard(user: User) {
    ContentCard(spacing = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                UserAvatar(user, size = 84.dp)
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(26.dp).background(Sage, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = "Cuenta verificada", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.name, color = WarmText, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                    Text(" ${user.city}", color = MutedText, fontSize = 13.sp)
                }
                Tag(
                    text = if (user.isModerator) "Moderador" else "${user.level.label} • Nivel ${user.level.number}",
                    icon = if (user.isModerator) Icons.Outlined.Shield else Icons.Outlined.WorkspacePremium,
                    container = TertiaryFixed,
                    content = OnTertiaryContainer
                )
            }
        }
        if (!user.isModerator) {
            LevelProgress(user)
            LevelRoute(user)
        }
    }
}

@Composable
private fun CommunityStats(stats: UserStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Estadísticas Comunitarias")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.AddCircleOutline, PrimaryFixed, Terracotta, "${stats.created}", "Eventos creados", Modifier.weight(1f))
            StatTile(Icons.Outlined.CheckCircle, SecondaryFixed, OnSecondaryFixedVariant, "${stats.finished}", "Finalizados con éxito", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.HourglassTop, TertiaryFixed, OnTertiaryContainer, "${stats.pending}", "En moderación", Modifier.weight(1f))
            StatTile(Icons.Outlined.EmojiEvents, PrimaryFixed, Terracotta, "${stats.checkIns}", "Asistencias con check-in", Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecentEventRow(event: CommunityEvent, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(event.status)
                Text(EventFormat.relative(event.createdAt), color = MutedText, fontSize = 12.sp)
            }
            Text(event.title, color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${event.address.substringBefore(",")} • ${EventFormat.shortDateTime(event.startsAt)}",
                color = MutedText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
