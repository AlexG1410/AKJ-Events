package co.edu.uniquindio.akjevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.edu.uniquindio.akjevents.core.navigation.AppNavigation
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AKJEventsTheme {
                AppNavigation()
            }
        }
    }
}
