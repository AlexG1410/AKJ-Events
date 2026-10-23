package co.edu.uniquindio.akjevents.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.EmptyState
import co.edu.uniquindio.akjevents.core.component.FilterPill
import co.edu.uniquindio.akjevents.core.component.MainTopBar
import co.edu.uniquindio.akjevents.core.component.Tag
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.NotificationGroup
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import java.time.LocalDate

private data class TypeStyle(val icon: ImageVector, val container: Color, val tint: Color)

private val NotificationType.style: TypeStyle
    get() = when (this) {
        NotificationType.EVENT_VERIFIED -> TypeStyle(Icons.Outlined.Verified, SecondaryFixed, OnSecondaryFixedVariant)
        NotificationType.EVENT_REJECTED -> TypeStyle(Icons.Outlined.Cancel, Color(0xFFFFDAD6), ErrorColor)
        NotificationType.NEW_COMMENT -> TypeStyle(Icons.AutoMirrored.Outlined.Chat, SurfaceContainerHigh, WarmText)
        NotificationType.ORGANIZER_NOTICE -> TypeStyle(Icons.Outlined.Campaign, PrimaryFixed, Terracotta)
        NotificationType.EVENT_FINISHED -> TypeStyle(Icons.Outlined.Flag, SurfaceContainerHigh, MutedText)
        NotificationType.REMINDER -> TypeStyle(Icons.Outlined.Schedule, PrimaryFixed, Terracotta)
        NotificationType.BADGE_UNLOCKED -> TypeStyle(Icons.Outlined.MilitaryTech, TertiaryFixed, OnTertiaryContainer)
        NotificationType.LEVEL_UP -> TypeStyle(Icons.Outlined.EmojiEvents, TertiaryFixed, OnTertiaryContainer)
    }

@Composable
fun NotificationsScreen(
    onOpenEvent: (String) -> Unit,
    onOpenProfile: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val today = LocalDate.now()
    val visible = state.visible
    val sections = listOf(
        "Hoy" to visible.filter { it.createdAt.toLocalDate() == today },
        "Esta semana" to visible.filter { it.createdAt.toLocalDate().isBefore(today) && it.createdAt.toLocalDate().isAfter(today.minusDays(7)) },
        "Anteriores" to visible.filter { !it.createdAt.toLocalDate().isAfter(today.minusDays(7)) }
    ).filter { it.second.isNotEmpty() }

    val open: (AppNotification) -> Unit = { notification ->
        viewModel.markAsRead(notification)
        when {
            notification.eventId != null -> onOpenEvent(notification.eventId)
            notification.type.group == NotificationGroup.ACHIEVEMENTS -> onOpenProfile()
        }
    }

    Scaffold(
        containerColor = WarmBackground,
        topBar = { MainTopBar(title = "Notificaciones", user = state.user, onAvatarClick = onOpenProfile) },
        bottomBar = bottomBar
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alertas", color = WarmText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                    if (state.unreadCount > 0) {
                        Tag("${state.unreadCount} nuevas", container = PrimaryFixed, content = Terracotta)
                    }
                    Box(Modifier.weight(1f))
                    IconButton(
                        onClick = viewModel::markAllAsRead,
                        enabled = state.unreadCount > 0,
                        modifier = Modifier.background(SurfaceContainer, CircleShape)
                    ) {
                        Icon(Icons.Outlined.DoneAll, contentDescription = "Marcar todas como leídas", tint = if (state.unreadCount > 0) Sage else MutedText)
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterPill(text = "Todas", count = state.count(null), isSelected = state.filter == null, onClick = { viewModel.selectFilter(null) })
                    }
                    items(NotificationGroup.entries) { group ->
                        FilterPill(
                            text = group.label,
                            count = state.count(group),
                            isSelected = state.filter == group,
                            onClick = { viewModel.selectFilter(group) }
                        )
                    }
                }
            }

            if (visible.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.NotificationsNone,
                        title = "Sin notificaciones",
                        message = "Aquí verás los comentarios en tus eventos, las decisiones de moderación y tus nuevos logros."
                    )
                }
            }

            sections.forEach { (title, notifications) ->
                item(key = "titulo-$title") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        val unread = notifications.count { !it.isRead }
                        if (unread > 0) Text("$unread pendientes", color = Terracotta, fontSize = 12.sp)
                    }
                }
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onOpen = { open(notification) },
                        onToggleRead = { viewModel.toggleRead(notification) }
                    )
                }
            }

            if (visible.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = Sage)
                        Column {
                            Text(
                                if (state.unreadCount == 0) "Estás al día en AKJ Events" else "Tienes ${state.unreadCount} sin leer",
                                color = WarmText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Las alertas se actualizan cada vez que entras a este apartado.", color = MutedText, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AppNotification, onOpen: () -> Unit, onToggleRead: () -> Unit) {
    val style = notification.type.style
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) SurfaceContainerLow else Color.White,
        shadowElevation = if (notification.isRead) 0.dp else 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).background(style.container, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(style.icon, contentDescription = null, tint = style.tint, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        notification.title,
                        color = WarmText,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold
                    )
                    Text(notification.message, color = MutedText, fontSize = 13.sp, lineHeight = 18.sp)
                    Text(EventFormat.relative(notification.createdAt), color = MutedText.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                if (!notification.isRead) {
                    Box(Modifier.padding(top = 4.dp).size(10.dp).background(Terracotta, CircleShape))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                Surface(onClick = onToggleRead, shape = CircleShape, color = SurfaceContainer) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (notification.isRead) Icons.Outlined.MarkEmailUnread else Icons.Outlined.MarkEmailRead,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(if (notification.isRead) "Marcar no leída" else "Marcar leída", color = MutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                val action = when {
                    notification.eventId != null -> "Ver evento"
                    notification.type.group == NotificationGroup.ACHIEVEMENTS -> "Ver mis insignias"
                    else -> null
                }
                if (action != null) {
                    Surface(onClick = onOpen, shape = CircleShape, color = Terracotta) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Outlined.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(action, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
