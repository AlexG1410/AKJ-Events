package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnPrimaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.WarmText

enum class BottomDestination(val label: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Outlined.Home),
    MAP("Mapa", Icons.Outlined.Map),
    CREATE("Crear", Icons.Outlined.Add),
    ALERTS("Alertas", Icons.Outlined.Notifications),
    PROFILE("Perfil", Icons.Outlined.Person)
}

/** Barra inferior del mockup 05: Inicio, Mapa, Crear (destacado), Alertas y Perfil. */
@Composable
fun AppBottomBar(
    selected: BottomDestination,
    onSelect: (BottomDestination) -> Unit,
    hasUnreadAlerts: Boolean = false
) {
    Surface(color = Color.White.copy(alpha = 0.95f), shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomDestination.entries.forEach { destination ->
                BottomBarItem(
                    destination = destination,
                    isSelected = destination == selected,
                    showBadge = destination == BottomDestination.ALERTS && hasUnreadAlerts,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: BottomDestination,
    isSelected: Boolean,
    showBadge: Boolean,
    onClick: () -> Unit
) {
    val isCreate = destination == BottomDestination.CREATE
    val pillColor = when {
        isCreate -> Terracotta
        isSelected -> PrimaryFixed
        else -> Color.Transparent
    }
    val iconColor = when {
        isCreate -> Color.White
        isSelected -> OnPrimaryFixedVariant
        else -> MutedText
    }

    Column(
        modifier = Modifier
            .width(56.dp)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 28.dp)
                .then(if (isCreate) Modifier.shadow(4.dp, CircleShape) else Modifier)
                .background(pillColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                destination.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(if (isCreate) 22.dp else 20.dp)
            )
            if (showBadge) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 4.dp)
                        .size(8.dp)
                        .background(Terracotta, CircleShape)
                )
            }
        }
        Text(
            destination.label,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isSelected) WarmText else MutedText
        )
    }
}
