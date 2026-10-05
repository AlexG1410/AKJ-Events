package co.edu.uniquindio.akjevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailScreen
import co.edu.uniquindio.akjevents.features.home.HomeScreen
import co.edu.uniquindio.akjevents.features.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainRoutes.Splash) {
        composable<MainRoutes.Splash> {
            // TODO: cambiar el destino a Login cuando exista esa pantalla
            SplashScreen(onFinished = {
                navController.navigate(MainRoutes.Home) {
                    // Saca el splash de la pila para que "atrás" no regrese a él
                    popUpTo<MainRoutes.Splash> { inclusive = true }
                }
            })
        }
        composable<MainRoutes.Home> {
            HomeScreen(onOpenEvent = { eventId ->
                navController.navigate(MainRoutes.EventDetail(eventId))
            })
        }
        composable<MainRoutes.EventDetail> { entry ->
            EventDetailScreen(
                eventId = entry.toRoute<MainRoutes.EventDetail>().eventId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
