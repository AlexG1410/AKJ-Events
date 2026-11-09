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
import androidx.compose.material.icons.outlined.WorkspacePremium
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import co.edu.uniquindio.akjevents.R
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
import co.edu.uniquindio.akjevents.core.util.labelRes
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
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var confirmLogout by remember { mutableStateOf(false) }
    val user = state.user

    Scaffold(
        containerColor = WarmBackground,
        topBar = { MainTopBar(title = stringResource(R.string.profile_title), user = user) },
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
                Text(stringResource(R.string.profile_title), color = WarmText, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                Tag(stringResource(R.string.profile_active), container = SecondaryFixed, content = OnSecondaryFixedVariant)
                Box(Modifier.weight(1f))
                val shareText = stringResource(R.string.profile_share_text, user.name, stringResource(user.level.labelRes), user.points)
                val shareTitle = stringResource(R.string.profile_share)
                RoundIconButton(Icons.Outlined.Share, shareTitle, onClick = { context.startActivity(shareProfileIntent(shareText, shareTitle)) })
                RoundIconButton(Icons.Outlined.Settings, stringResource(R.string.profile_edit), onClick = onEditProfile)
            }

            ProfileHeaderCard(user)

            if (user.isModerator) {
                ContentCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).background(SecondaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Shield, contentDescription = null, tint = OnSecondaryFixedVariant)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.profile_moderator_account), color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.profile_moderator_description),
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
                        stringResource(R.string.profile_badges),
                        action = stringResource(R.string.profile_badges_count, user.badges.size, Badge.entries.size),
                        onAction = onOpenStats
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Badge.entries.forEach { badge ->
                            BadgeItem(badge, unlocked = badge in user.badges, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(stringResource(R.string.profile_my_events), action = stringResource(R.string.profile_manage), onAction = onOpenMyEvents)
                    if (state.recentEvents.isEmpty()) {
                        Text(stringResource(R.string.profile_no_events), color = MutedText, fontSize = 13.sp)
                    }
                    state.recentEvents.forEach { event -> RecentEventRow(event, onClick = { onOpenEvent(event.id) }) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(stringResource(R.string.profile_settings))
                ContentCard(spacing = 0.dp, padding = 8.dp) {
                    MenuRow(Icons.Outlined.ManageAccounts, PrimaryFixed, Terracotta, stringResource(R.string.profile_edit), stringResource(R.string.profile_edit_description), onEditProfile)
                    if (user.isModerator) {
                        MenuRow(Icons.Outlined.Shield, SecondaryFixed, OnSecondaryFixedVariant, stringResource(R.string.profile_moderation_panel), stringResource(R.string.profile_moderation_description), onOpenModeration)
                    } else {
                        MenuRow(Icons.AutoMirrored.Outlined.ShowChart, SecondaryFixed, OnSecondaryFixedVariant, stringResource(R.string.profile_stats), stringResource(R.string.profile_stats_description), onOpenStats)
                        MenuRow(Icons.Outlined.QrCode2, SurfaceContainerHigh, WarmText, stringResource(R.string.profile_my_qr), stringResource(R.string.profile_my_qr_description), onOpenMyQr)
                        MenuRow(Icons.Outlined.EventAvailable, TertiaryFixed, OnTertiaryContainer, stringResource(R.string.profile_my_events_menu), stringResource(R.string.profile_my_events_description), onOpenMyEvents)
                    }
                    MenuRow(Icons.Outlined.Notifications, SurfaceContainer, WarmText, stringResource(R.string.notifications_title), stringResource(R.string.profile_notifications_description), onOpenNotifications)
                    MenuRow(
                        Icons.AutoMirrored.Outlined.Logout,
                        Color(0xFFFFDAD6),
                        ErrorColor,
                        stringResource(R.string.profile_logout),
                        stringResource(R.string.profile_logout_description),
                        onClick = { confirmLogout = true },
                        titleColor = ErrorColor
                    )
                }
            }

            Text(
                stringResource(R.string.profile_footer),
                color = MutedText,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.profile_logout_title),
            message = stringResource(R.string.profile_logout_message),
            confirmText = stringResource(R.string.profile_logout),
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

private fun shareProfileIntent(text: String, title: String): Intent {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, title)
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
                    Icon(Icons.Outlined.Verified, contentDescription = stringResource(R.string.profile_verified_account), tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(user.name, color = WarmText, fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Terracotta, modifier = Modifier.size(14.dp))
                    Text(" ${user.city}", color = MutedText, fontSize = 13.sp)
                }
                Tag(
                    text = if (user.isModerator) stringResource(R.string.profile_moderator) else stringResource(R.string.my_events_level, stringResource(user.level.labelRes), user.level.number),
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
        SectionTitle(stringResource(R.string.profile_community_stats))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.AddCircleOutline, PrimaryFixed, Terracotta, "${stats.created}", stringResource(R.string.profile_events_created), Modifier.weight(1f))
            StatTile(Icons.Outlined.CheckCircle, SecondaryFixed, OnSecondaryFixedVariant, "${stats.finished}", stringResource(R.string.profile_finished_success), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.HourglassTop, TertiaryFixed, OnTertiaryContainer, "${stats.pending}", stringResource(R.string.profile_in_moderation), Modifier.weight(1f))
            StatTile(Icons.Outlined.EmojiEvents, PrimaryFixed, Terracotta, "${stats.checkIns}", stringResource(R.string.profile_checked_in), Modifier.weight(1f))
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
                stringResource(R.string.moderation_date_time, event.address.substringBefore(","), EventFormat.shortDateTime(event.startsAt)),
                color = MutedText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
