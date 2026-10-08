package co.edu.uniquindio.akjevents.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.features.auth.login.LoginScreen
import co.edu.uniquindio.akjevents.features.auth.recover.RecoverPasswordScreen
import co.edu.uniquindio.akjevents.features.auth.register.RegisterScreen
import co.edu.uniquindio.akjevents.features.event.create.CreateEventScreen
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailScreen
import co.edu.uniquindio.akjevents.features.home.HomeScreen
import co.edu.uniquindio.akjevents.features.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainRoutes.Splash) {
        composable<MainRoutes.Splash> { entry ->
            SplashScreen(onFinished = {
                navController.fromTop(entry) {
                    navigate(MainRoutes.Login) {
                        // Saca el splash de la pila para que "atrás" no regrese a él
                        popUpTo<MainRoutes.Splash> { inclusive = true }
                    }
                }
            })
        }
        composable<MainRoutes.Login> { entry ->
            LoginScreen(
                onLoginSuccess = { navController.fromTop(entry) { navigateToHome() } },
                onCreateAccount = { navController.fromTop(entry) { navigate(MainRoutes.Register) } },
                onForgotPassword = { navController.fromTop(entry) { navigate(MainRoutes.RecoverPassword) } }
            )
        }
        composable<MainRoutes.Register> { entry ->
            RegisterScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onRegisterSuccess = { navController.fromTop(entry) { navigateToHome() } }
            )
        }
        composable<MainRoutes.RecoverPassword> { entry ->
            RecoverPasswordScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onLinkSent = { navController.fromTop(entry) { popBackStack() } }
            )
        }
        composable<MainRoutes.Home> { entry ->
            HomeScreen(
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onCreateEvent = { navController.fromTop(entry) { navigate(MainRoutes.CreateEvent) } }
            )
        }
        composable<MainRoutes.EventDetail> { entry ->
            EventDetailScreen(
                eventId = entry.toRoute<MainRoutes.EventDetail>().eventId,
                onBack = { navController.fromTop(entry) { popBackStack() } }
            )
        }
        composable<MainRoutes.CreateEvent> { entry ->
            CreateEventScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                // El evento queda pendiente de verificación, así que el feed no lo muestra
                onEventSubmitted = { navController.fromTop(entry) { popBackStack(MainRoutes.Home, inclusive = false) } }
            )
        }
    }
}

/**
 * Navega solo si [entry] sigue siendo la pantalla de arriba de la pila. Evita que un doble toque
 * abra dos veces una pantalla o saque también la anterior (dejando la app en blanco), y que una
 * confirmación tardía navegue cuando el usuario ya salió con "atrás".
 */
private inline fun NavHostController.fromTop(entry: NavBackStackEntry, action: NavHostController.() -> Unit) {
    if (currentBackStackEntry?.id == entry.id) action()
}

/** Va al Home sacando de la pila el Login y lo que haya encima (Registro). */
private fun NavHostController.navigateToHome() {
    navigate(MainRoutes.Home) {
        popUpTo<MainRoutes.Login> { inclusive = true }
        launchSingleTop = true
    }
}
