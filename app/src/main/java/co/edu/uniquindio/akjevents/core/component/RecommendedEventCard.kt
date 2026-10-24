package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.core.util.remainingSpots
import co.edu.uniquindio.akjevents.domain.model.RecommendedEvent

/** La misma tarjeta en el resumen de Inicio y en la pantalla completa. */
@Composable
fun RecommendedEventCard(item: RecommendedEvent, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val event = item.event
    Surface(onClick = onOpen, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 1.dp) {
        Column {
            Box(Modifier.fillMaxWidth().height(130.dp)) {
                EventImage(event, Modifier.fillMaxSize())
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CategoryChip(event.category)
                    VerifiedBadge(iconSize = 12.dp)
                }
                Text(event.title, color = WarmText, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(EventFormat.weekdayDateTime(event.startsAt), color = MutedText, fontSize = 12.sp)
                Text("${event.address} • a ${EventFormat.distance(item.distanceKm)}", color = MutedText, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(event.remainingSpots?.let { "$it cupos disponibles" } ?: "Cupo abierto", color = Sage, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(8.dp)) {
                    Text(item.reason, modifier = Modifier.fillMaxWidth().padding(10.dp), color = WarmText, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}
