package co.edu.uniquindio.akjevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.features.auth.login.LoginScreen
import co.edu.uniquindio.akjevents.features.auth.recover.RecoverPasswordScreen
import co.edu.uniquindio.akjevents.features.auth.register.RegisterScreen
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailScreen
import co.edu.uniquindio.akjevents.features.home.HomeScreen
import co.edu.uniquindio.akjevents.features.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainRoutes.Splash) {
        composable<MainRoutes.Splash> {
            SplashScreen(onFinished = {
                navController.navigate(MainRoutes.Login) {
                    // Saca el splash de la pila para que "atrás" no regrese a él
                    popUpTo<MainRoutes.Splash> { inclusive = true }
                }
            })
        }
        composable<MainRoutes.Login> {
            LoginScreen(
                onLoginSuccess = { navController.navigateToHome() },
                onCreateAccount = {
                    navController.navigate(MainRoutes.Register) { launchSingleTop = true }
                },
                onForgotPassword = {
                    navController.navigate(MainRoutes.RecoverPassword) { launchSingleTop = true }
                }
            )
        }
        composable<MainRoutes.Register> {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigateToHome() }
            )
        }
        composable<MainRoutes.RecoverPassword> {
            RecoverPasswordScreen(
                onBack = { navController.popBackStack() },
                onLinkSent = { navController.popBackStack() }
            )
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

/** Va al Home sacando de la pila el Login y lo que haya encima (Registro). */
private fun NavHostController.navigateToHome() {
    navigate(MainRoutes.Home) {
        popUpTo<MainRoutes.Login> { inclusive = true }
        launchSingleTop = true
    }
}
