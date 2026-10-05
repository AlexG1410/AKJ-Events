package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHighest
import co.edu.uniquindio.akjevents.domain.model.EventCategory

/** Insignia "Verificado" de las tarjetas de evento. */
@Composable
fun VerifiedBadge(modifier: Modifier = Modifier, iconSize: Dp = 14.dp) {
    Row(
        modifier = modifier
            .background(SecondaryFixed, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(Icons.Outlined.Verified, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(iconSize))
        Text("Verificado", color = OnSecondaryFixedVariant, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
    }
}

/** Etiqueta de categoría con el color que le asignan los mockups. */
@Composable
fun CategoryChip(category: EventCategory, modifier: Modifier = Modifier, showIcon: Boolean = false) {
    val style = category.style
    Row(
        modifier = modifier
            .background(style.containerColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = if (showIcon) 4.dp else 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showIcon) {
            Icon(style.icon, contentDescription = null, tint = style.contentColor, modifier = Modifier.size(14.dp))
        }
        Text(category.label, color = style.contentColor, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Barra redondeada del cupo ocupado (0..1). */
@Composable
fun CapacityBar(progress: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(SurfaceContainerHighest)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}
