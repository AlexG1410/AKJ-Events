package co.edu.uniquindio.akjevents.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import coil3.compose.AsyncImage
import java.time.format.DateTimeFormatter
import java.util.Locale

private val eventDateFormat = DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", Locale.forLanguageTag("es-CO"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("AKJ Events", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Descubre tu comunidad", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Eventos para encontrarnos en Armenia y sus alrededores.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.category == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Todos") }
                        )
                    }
                    items(EventCategory.entries) { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.label) }
                        )
                    }
                }
            }

            item {
                Text("Próximos eventos", style = MaterialTheme.typography.titleLarge)
            }

            if (state.events.isEmpty()) {
                item { Text("Aún no hay eventos en esta categoría.") }
            }

            items(state.events, key = { it.id }) { event ->
                EventCard(event = event, onClick = { onOpenEvent(event.id) })
            }
        }
    }
}

@Composable
private fun EventCard(event: CommunityEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            model = event.imageUrls.first(),
            contentDescription = "Imagen del evento ${event.title}",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.event_placeholder),
            error = painterResource(R.drawable.event_placeholder),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        )
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(event.category.label, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                Text("Verificado", color = MaterialTheme.colorScheme.secondary)
            }
            Text(event.title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(event.startsAt.format(eventDateFormat), style = MaterialTheme.typography.bodyMedium)
            Text(
                event.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${event.attendanceCount} personas asistirán · ${event.interestCount} interesadas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
