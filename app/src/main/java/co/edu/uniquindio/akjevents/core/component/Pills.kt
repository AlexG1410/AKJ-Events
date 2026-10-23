package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.theme.ErrorColor
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.domain.model.EventStatus

/** Píldora de filtro (categorías, pestañas de estado...). Con [count] muestra un contador. */
@Composable
fun FilterPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    count: Int? = null,
    selectedColor: Color = Sage
) {
    val contentColor = if (isSelected) Color.White else WarmText
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) selectedColor else SurfaceContainer,
        shadowElevation = if (isSelected) 1.dp else 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.let { Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp)) }
            Text(text, color = contentColor, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold)
            if (count != null) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 20.dp)
                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else SurfaceContainerHigh, CircleShape)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$count", color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Etiqueta con el estado de moderación de un evento. */
@Composable
fun StatusBadge(status: EventStatus, modifier: Modifier = Modifier) {
    val (icon, container, content) = when (status) {
        EventStatus.PENDING -> Triple(Icons.Outlined.HourglassTop, TertiaryFixed, OnTertiaryContainer)
        EventStatus.VERIFIED -> Triple(Icons.Outlined.Verified, SecondaryFixed, OnSecondaryFixedVariant)
        EventStatus.REJECTED -> Triple(Icons.Outlined.Cancel, Color(0xFFFFDAD6), ErrorColor)
        EventStatus.FINISHED -> Triple(Icons.Outlined.Flag, SurfaceContainerHigh, MutedText)
    }
    Tag(text = status.label, icon = icon, container = container, content = content, modifier = modifier)
}

/** Etiqueta redondeada pequeña con ícono opcional. */
@Composable
fun Tag(text: String, container: Color, content: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Row(
        modifier = modifier
            .background(container, CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(14.dp)) }
        Text(text, color = content, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
    }
}
