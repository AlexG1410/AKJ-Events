package co.edu.uniquindio.akjevents.features.event.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import coil3.compose.AsyncImage
import java.time.format.DateTimeFormatter
import java.util.Locale

private val detailDateFormat = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM · h:mm a", Locale.forLanguageTag("es-CO"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(eventId: String, onBack: () -> Unit) {
    val event = SampleEvents.findPublicById(eventId)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Detalle del evento") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (event == null) {
            Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                Text("Este evento no está disponible.", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onBack) { Text("Volver al inicio") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AsyncImage(
                        model = event.imageUrls.first(),
                        contentDescription = "Imagen del evento ${event.title}",
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.event_placeholder),
                        error = painterResource(R.drawable.event_placeholder),
                        modifier = Modifier.fillMaxWidth().height(230.dp)
                    )
                }
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${event.category.label} · Evento verificado", color = MaterialTheme.colorScheme.secondary)
                        Text(event.title, style = MaterialTheme.typography.headlineMedium)
                        Text("Inicio: ${event.startsAt.format(detailDateFormat)}")
                        Text("Fin: ${event.endsAt.format(detailDateFormat)}")
                        Text(event.address, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Coordenadas: ${event.location.latitude}, ${event.location.longitude}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Acerca de este evento", style = MaterialTheme.typography.titleLarge)
                        Text(event.description, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("${event.attendanceCount} personas han confirmado asistencia", style = MaterialTheme.typography.titleMedium)
                        Text(
                            event.capacity?.let { "Cupo máximo: $it personas" } ?: "Sin límite de cupos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
