package co.edu.uniquindio.akjevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailScreen
import co.edu.uniquindio.akjevents.features.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainRoutes.Home) {
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
