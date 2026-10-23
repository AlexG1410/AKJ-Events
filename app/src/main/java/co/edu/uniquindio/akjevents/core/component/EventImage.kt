package co.edu.uniquindio.akjevents.core.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import coil3.compose.AsyncImage

/** Primera imagen del evento, con la imagen local de respaldo si no carga. */
@Composable
fun EventImage(event: CommunityEvent, modifier: Modifier = Modifier) {
    AsyncImage(
        model = event.imageUrls.first(),
        contentDescription = "Imagen del evento ${event.title}",
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.event_placeholder),
        error = painterResource(R.drawable.event_placeholder),
        modifier = modifier
    )
}
