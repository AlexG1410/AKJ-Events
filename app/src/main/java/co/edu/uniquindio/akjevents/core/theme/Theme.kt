package co.edu.uniquindio.akjevents.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AkjColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    secondary = Sage,
    onSecondary = Color.White,
    tertiary = Honey,
    background = WarmBackground,
    onBackground = WarmText,
    surface = WarmSurface,
    onSurface = WarmText,
    onSurfaceVariant = MutedText,
    outline = SoftBorder
)

@Composable
fun AKJEventsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AkjColors, typography = Typography, content = content)
}
