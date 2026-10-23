package co.edu.uniquindio.akjevents.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import kotlin.math.roundToInt

/** Progreso hacia el siguiente nivel, compartido por el perfil y las estadísticas. */
@Composable
fun LevelProgress(user: User) {
    val next = user.level.next
    val progress = if (next == null) 1f else
        (user.points - user.level.minPoints).toFloat() / (next.minPoints - user.level.minPoints)
    Column(
        modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(28.dp).background(PrimaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = Terracotta, modifier = Modifier.size(16.dp))
            }
            Text("${user.points} Puntos Cívicos", color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                if (next == null) "Nivel máximo" else "${user.points} / ${next.minPoints} pts • ${(progress * 100).roundToInt()}%",
                color = MutedText,
                fontSize = 12.sp
            )
        }
        CapacityBar(progress = progress, color = Terracotta, height = 8.dp)
        Text(
            if (next == null) "¡Alcanzaste el rango máximo de Líder Comunitario!"
            else "Estás a ${next.minPoints - user.points} pts de alcanzar el rango de ${next.label} (${next.minPoints} pts).",
            color = WarmText,
            fontSize = 12.sp
        )
        Text(
            "* Los rangos reflejan tu reputación en la comunidad; no dan permisos de moderación.",
            color = MutedText,
            fontSize = 11.sp
        )
    }
}

/** "Ruta de liderazgo local": los 4 niveles con el actual resaltado. */
@Composable
fun LevelRoute(user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("RUTA DE LIDERAZGO LOCAL", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            UserLevel.entries.forEach { level ->
                val isCurrent = level == user.level
                val reached = level.ordinal < user.level.ordinal
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isCurrent) SecondaryFixed else SurfaceContainer, RoundedCornerShape(10.dp))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("N${level.number}", color = if (isCurrent) OnSecondaryFixedVariant else MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        level.label,
                        color = if (isCurrent) OnSecondaryFixedVariant else WarmText,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    when {
                        reached -> Icon(Icons.Outlined.CheckCircle, contentDescription = "Alcanzado", tint = OnSecondaryFixedVariant, modifier = Modifier.size(14.dp))
                        isCurrent -> Text("${user.points} pts", color = OnSecondaryFixedVariant, fontSize = 10.sp)
                        else -> Text("+${level.minPoints - user.points}", color = Terracotta, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

val Badge.icon: ImageVector
    get() = when (this) {
        Badge.FIRST_EVENT -> Icons.Outlined.Flag
        Badge.FIRST_COMMENT -> Icons.AutoMirrored.Outlined.Chat
        Badge.FIRST_CHECK_IN -> Icons.Outlined.QrCodeScanner
        Badge.TEN_VERIFIED -> Icons.Outlined.Verified
        Badge.FEATURED_OF_MONTH -> Icons.Outlined.Star
    }

/** Insignia desbloqueada (a color) o bloqueada (gris con candado). */
@Composable
fun BadgeItem(badge: Badge, unlocked: Boolean, modifier: Modifier = Modifier) {
    val colors = listOf(PrimaryFixed to Terracotta, SecondaryFixed to OnSecondaryFixedVariant, TertiaryFixed to OnTertiaryContainer)
    val (container, tint) = if (unlocked) colors[badge.ordinal % colors.size] else SurfaceContainer to MutedText.copy(alpha = 0.6f)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(52.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
            Icon(if (unlocked) badge.icon else Icons.Outlined.Lock, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Text(badge.label, color = if (unlocked) WarmText else MutedText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 13.sp)
        Text(
            if (unlocked) "Obtenida" else "Bloqueada",
            color = if (unlocked) OnSecondaryFixedVariant else MutedText.copy(alpha = 0.7f),
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

/** Tarjeta de una métrica: ícono, número grande y etiqueta (mockups 12 y 15). */
@Composable
fun StatTile(
    icon: ImageVector,
    container: Color,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(container, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Box(Modifier.weight(1f))
            caption?.let { Text(it, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
        Text(value, color = WarmText, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text(label, color = MutedText, fontSize = 12.sp, lineHeight = 15.sp)
    }
}
