package co.edu.uniquindio.akjevents.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.uniquindio.akjevents.core.theme.Honey
import co.edu.uniquindio.akjevents.core.theme.MutedText
import co.edu.uniquindio.akjevents.core.theme.OnSecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.OnTertiaryContainer
import co.edu.uniquindio.akjevents.core.theme.Sage
import co.edu.uniquindio.akjevents.core.theme.SecondaryContainer
import co.edu.uniquindio.akjevents.core.theme.SurfaceContainer
import co.edu.uniquindio.akjevents.core.theme.TertiaryColor
import co.edu.uniquindio.akjevents.domain.model.EventCategory

/** Colores e ícono con que los mockups 05 y 06 muestran cada categoría. */
data class CategoryStyle(val icon: ImageVector, val containerColor: Color, val contentColor: Color, val emoji: String)

val EventCategory.style: CategoryStyle
    get() = when (this) {
        EventCategory.SPORTS -> CategoryStyle(Icons.AutoMirrored.Outlined.DirectionsBike, Sage, Color.White, "⚽")
        EventCategory.CULTURE -> CategoryStyle(Icons.Outlined.Palette, SurfaceContainer, MutedText, "🎨")
        EventCategory.ACADEMIC -> CategoryStyle(Icons.Outlined.School, Honey, OnTertiaryContainer, "📚")
        EventCategory.VOLUNTEERING -> CategoryStyle(Icons.Outlined.VolunteerActivism, SecondaryContainer, OnSecondaryContainer, "🤝")
        EventCategory.SOCIAL -> CategoryStyle(Icons.Outlined.LocalCafe, TertiaryColor, Color.White, "☕")
    }
