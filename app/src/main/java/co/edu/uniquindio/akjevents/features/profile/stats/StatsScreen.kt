package co.edu.uniquindio.akjevents.features.profile.stats

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.core.component.CapacityBar
import co.edu.uniquindio.akjevents.core.component.ContentCard
import co.edu.uniquindio.akjevents.core.component.EventImage
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.SectionTitle
import co.edu.uniquindio.akjevents.core.component.Tag
import co.edu.uniquindio.akjevents.core.theme.Honey
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryFixedVariant
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.PrimaryFixed
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.SecondaryFixed
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerHigh
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainerLow
import co.edu.uniquindio.akjevents.core.theme.Terracotta
import co.edu.uniquindio.akjevents.core.theme.TertiaryColor
import co.edu.uniquindio.akjevents.core.theme.TertiaryFixed
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.EventFormat
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.features.profile.LevelProgress
import co.edu.uniquindio.akjevents.features.profile.StatTile
import co.edu.uniquindio.akjevents.features.profile.UserStats
import kotlin.math.roundToInt

/** Colores de cada categoría en la gráfica de compromiso. */
private val EventCategory.chartColor: Color
    get() = when (this) {
        EventCategory.SPORTS -> Terracotta
        EventCategory.CULTURE -> Honey
        EventCategory.ACADEMIC -> Sage
        EventCategory.VOLUNTEERING -> OnSecondaryContainer
        EventCategory.SOCIAL -> TertiaryColor
    }

@Composable
fun StatsScreen(onBack: () -> Unit, onOpenEvent: (String) -> Unit, viewModel: StatsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val user = state.user

    Scaffold(containerColor = WarmBackground) { innerPadding ->
        if (user == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ScreenHeader(title = "Mis Estadísticas", subtitle = "Balance cívico y participación activa", onBack = onBack)

            ReputationCard(user)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Actividad en convocatorias")
                ActivityGrid(state.stats)
            }

            CommitmentCard(state.stats)

            if (state.pointsHistory.isNotEmpty()) {
                PointsHistory(state.pointsHistory)
            }

            if (state.finishedEvents.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Tus huellas en el barrio")
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.finishedEvents.forEach { event ->
                            Surface(onClick = { onOpenEvent(event.id) }, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(width = 170.dp, height = 110.dp)) {
                                Box {
                                    EventImage(event, Modifier.fillMaxSize())
                                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)))))
                                    Text(
                                        event.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(16.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(40.dp).background(SurfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Share, contentDescription = null, tint = Terracotta)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Comparte tu impacto", color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Incentiva a más vecinos con tu ejemplo", color = MutedText, fontSize = 12.sp)
                }
                Surface(onClick = { context.startActivity(shareImpactIntent(user, state.stats)) }, shape = RoundedCornerShape(12.dp), color = Terracotta) {
                    Text("Compartir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                }
            }
        }
    }
}

private fun shareImpactIntent(user: User, stats: UserStats): Intent {
    val text = "En AKJ Events soy ${user.level.label} con ${user.points} puntos cívicos: organicé ${stats.created} eventos " +
        "y registré ${stats.checkIns} asistencias. ¡Súmate a la comunidad!"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, "Compartir impacto")
}

@Composable
private fun ReputationCard(user: User) {
    ContentCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(52.dp).background(SecondaryFixed, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.WorkspacePremium, contentDescription = null, tint = OnSecondaryFixedVariant, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("REPUTACIÓN CIUDADANA", color = Sage, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(user.level.label, color = WarmText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
            Tag("Nivel ${user.level.number}", container = PrimaryFixed, content = Terracotta)
        }
        LevelProgress(user)
        if (user.badges.isNotEmpty()) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                user.badges.forEach { badge ->
                    Tag(badge.label, container = TertiaryFixed, content = OnTertiaryContainer, icon = Icons.Outlined.EmojiEvents)
                }
            }
        }
    }
}

@Composable
private fun ActivityGrid(stats: UserStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.PlayCircle, SecondaryFixed, OnSecondaryFixedVariant, "${stats.active}", "Eventos activos (verificados)", Modifier.weight(1f), caption = "En curso")
            StatTile(Icons.Outlined.CheckCircle, SurfaceContainerHigh, WarmText, "${stats.finished}", "Eventos concluidos", Modifier.weight(1f), caption = "Histórico")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.Outlined.HourglassTop, TertiaryFixed, OnTertiaryContainer, "${stats.pending}", "Por verificar", Modifier.weight(1f), caption = "Revisión")
            StatTile(Icons.Outlined.QrCodeScanner, PrimaryFixed, Terracotta, "${stats.checkIns}", "Asistencias con QR", Modifier.weight(1f), caption = "Presencial")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(Icons.AutoMirrored.Outlined.Chat, SurfaceContainerHigh, WarmText, "${stats.comments}", "Comentarios cívicos", Modifier.weight(1f), caption = "Aportes")
            StatTile(Icons.Outlined.Groups, SecondaryContainer, OnSecondaryContainer, "${stats.interestedNeighbors}", "Vecinos interesados en tus eventos", Modifier.weight(1f), caption = "Alcance")
        }
    }
}

@Composable
private fun CommitmentCard(stats: UserStats) {
    ContentCard(spacing = 16.dp) {
        Column {
            Text("Compromiso Comunitario", color = WarmText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Distribución de tus eventos por categoría", color = MutedText, fontSize = 12.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(SurfaceContainerLow, RoundedCornerShape(12.dp)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tasa de cumplimiento", color = WarmText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text("Asistencias confirmadas que terminaron en check-in", color = MutedText, fontSize = 12.sp)
            }
            Text(stats.complianceRate?.let { "$it%" } ?: "—", color = Sage, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        val total = stats.eventsByCategory.values.sum()
        if (total == 0) {
            Text("Cuando crees eventos verás aquí cómo se reparten por categoría.", color = MutedText, fontSize = 13.sp)
        } else {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 22.dp.toPx()
                    var start = -90f
                    stats.eventsByCategory.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                        val sweep = 360f * count / total
                        drawArc(
                            color = category.chartColor,
                            startAngle = start,
                            sweepAngle = sweep - 2f,
                            useCenter = false,
                            topLeft = Offset(stroke / 2, stroke / 2),
                            size = Size(size.width - stroke, size.height - stroke),
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$total", color = WarmText, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Total", color = MutedText, fontSize = 12.sp)
                }
            }
            stats.eventsByCategory.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                val share = count.toFloat() / total
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(category.chartColor))
                        Text("  ${category.label}", color = WarmText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text("${(share * 100).roundToInt()}%", color = WarmText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    CapacityBar(progress = share, color = category.chartColor, height = 6.dp)
                }
            }
        }
    }
}

@Composable
private fun PointsHistory(history: List<PointsEntry>) {
    ContentCard(spacing = 10.dp) {
        SectionTitle("Historial de puntos")
        history.take(HISTORY_LIMIT).forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.reason, color = WarmText, fontSize = 13.sp)
                    Text(EventFormat.relative(entry.createdAt), color = MutedText, fontSize = 11.sp)
                }
                Text(
                    if (entry.points >= 0) "+${entry.points} pts" else "${entry.points} pts",
                    color = if (entry.points >= 0) Sage else Terracotta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(72.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private const val HISTORY_LIMIT = 12
