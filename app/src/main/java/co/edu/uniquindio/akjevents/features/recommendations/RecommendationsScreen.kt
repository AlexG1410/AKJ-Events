package co.edu.uniquindio.akjevents.features.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.component.ContentCard
import co.edu.uniquindio.akjevents.core.component.RecommendedEventCard
import co.edu.uniquindio.akjevents.core.component.ScreenHeader
import co.edu.uniquindio.akjevents.core.component.SectionTitle
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.WarmBackground

@Composable
fun RecommendationsScreen(onBack: () -> Unit, onOpenEvent: (String) -> Unit, viewModel: RecommendationsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val feed = viewModel.visibleFeed(state.feed)
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(containerColor = WarmBackground) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScreenHeader(title = "Recomendados para ti", onBack = onBack, subtitle = "Selección local según tus preferencias y actividad") }
            when {
                state.isLoading -> item { CircularProgressIndicator() }
                feed.items.isEmpty() -> item { RecommendationsEmpty() }
                else -> {
                    if (!feed.hasPersonalization) item { DiscoveryHint() }
                    items(feed.items, key = { it.event.id }) { item ->
                        RecommendedEventCard(item, onOpen = {
                            if (viewModel.visibleFeed(state.feed).items.any { it.event.id == item.event.id }) onOpenEvent(item.event.id)
                            else viewModel.refresh()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationsPreview(viewModel: RecommendationsViewModel, onViewAll: () -> Unit, onOpenEvent: (String) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val feed = viewModel.visibleFeed(state.feed)
    LaunchedEffect(Unit) { viewModel.refresh() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Recomendados para ti", modifier = Modifier.padding(horizontal = 16.dp), action = "Ver todos", onAction = onViewAll)
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
            feed.items.isEmpty() -> RecommendationsEmpty(Modifier.padding(horizontal = 16.dp))
            else -> {
                if (!feed.hasPersonalization) DiscoveryHint(Modifier.padding(horizontal = 16.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(feed.items.take(3), key = { it.event.id }) { item ->
                        RecommendedEventCard(item, onOpen = {
                            if (viewModel.visibleFeed(state.feed).items.any { it.event.id == item.event.id }) onOpenEvent(item.event.id)
                            else viewModel.refresh()
                        }, modifier = Modifier.fillParentMaxWidth(0.86f).widthIn(max = 340.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveryHint(modifier: Modifier = Modifier) {
    Text("Para empezar, te mostramos opciones cercanas y variadas. Elige intereses en tu perfil o marca «Me interesa» para afinar la selección.", modifier = modifier.fillMaxWidth(), color = MutedText, fontSize = 13.sp)
}

@Composable
private fun RecommendationsEmpty(modifier: Modifier = Modifier) {
    ContentCard(modifier) {
        SectionTitle("No hay nuevas recomendaciones")
        Text("Por ahora no hay eventos próximos con cupo que puedas descubrir. Los tuyos y aquellos a los que ya confirmaste asistencia no se incluyen. Vuelve más tarde para ver nuevas publicaciones verificadas.", color = MutedText, fontSize = 13.sp)
    }
}
