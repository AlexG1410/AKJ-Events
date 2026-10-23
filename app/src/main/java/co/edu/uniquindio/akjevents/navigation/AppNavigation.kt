package co.edu.uniquindio.akjevents.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.core.component.AppBottomBar
import co.edu.uniquindio.akjevents.core.component.BottomDestination
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.features.auth.login.LoginScreen
import co.edu.uniquindio.akjevents.features.auth.recover.RecoverPasswordScreen
import co.edu.uniquindio.akjevents.features.auth.register.RegisterScreen
import co.edu.uniquindio.akjevents.features.checkin.CheckInScreen
import co.edu.uniquindio.akjevents.features.event.create.CreateEventScreen
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailScreen
import co.edu.uniquindio.akjevents.features.home.HomeScreen
import co.edu.uniquindio.akjevents.features.map.explore.MapScreen
import co.edu.uniquindio.akjevents.features.map.pick.PickLocationScreen
import co.edu.uniquindio.akjevents.features.moderation.ModerationScreen
import co.edu.uniquindio.akjevents.features.myevents.MyEventsScreen
import co.edu.uniquindio.akjevents.features.notifications.NotificationsScreen
import co.edu.uniquindio.akjevents.features.profile.ProfileScreen
import co.edu.uniquindio.akjevents.features.profile.edit.EditProfileScreen
import co.edu.uniquindio.akjevents.features.profile.stats.StatsScreen
import co.edu.uniquindio.akjevents.features.splash.SplashScreen

/** Clave con la que el selector de ubicación devuelve el punto elegido a la pantalla anterior. */
private const val PICKED_LOCATION_KEY = "picked_location"

@Composable
fun AppNavigation(session: SessionViewModel = viewModel()) {
    val navController = rememberNavController()
    val sessionState by session.uiState.collectAsState()

    // Raíz de las pestañas: el moderador entra a su panel; el usuario, al feed
    val rootRoute: MainRoutes = if (sessionState.isModerator) MainRoutes.Moderation else MainRoutes.Home
    val defaultCenter = sessionState.user?.location ?: Location(4.5339, -75.6811)

    val bottomBar: @Composable (BottomDestination?) -> Unit = { selected ->
        AppBottomBar(
            selected = selected,
            onSelect = { navController.openTab(it, rootRoute) },
            hasUnreadAlerts = sessionState.unreadAlerts > 0,
            isModerator = sessionState.isModerator
        )
    }

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
                onLoginSuccess = { isModerator -> navController.fromTop(entry) { navigateAfterLogin(isModerator) } },
                onCreateAccount = { navController.fromTop(entry) { navigate(MainRoutes.Register) } },
                onForgotPassword = { navController.fromTop(entry) { navigate(MainRoutes.RecoverPassword) } }
            )
        }
        composable<MainRoutes.Register> { entry ->
            RegisterScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                // El registro siempre crea cuentas de Usuario; los moderadores están precargados
                onRegisterSuccess = { navController.fromTop(entry) { navigateAfterLogin(isModerator = false) } }
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
                onCreateEvent = { navController.openTab(BottomDestination.CREATE, rootRoute) },
                onOpenMap = { navController.openTab(BottomDestination.MAP, rootRoute) },
                onOpenProfile = { navController.openTab(BottomDestination.PROFILE, rootRoute) },
                onOpenMyEvents = { navController.fromTop(entry) { navigate(MainRoutes.MyEvents) } },
                onChangeCity = { navController.fromTop(entry) { navigate(MainRoutes.EditProfile) } },
                bottomBar = { bottomBar(BottomDestination.HOME) }
            )
        }
        composable<MainRoutes.EventDetail> { entry ->
            EventDetailScreen(
                eventId = entry.toRoute<MainRoutes.EventDetail>().eventId,
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onEditEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EditEvent(eventId)) } },
                onOpenCheckIn = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.CheckIn(eventId = eventId)) } }
            )
        }
        composable<MainRoutes.CreateEvent> { entry ->
            val picked by entry.savedStateHandle.getStateFlow<String?>(PICKED_LOCATION_KEY, null).collectAsState()
            CreateEventScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                // El evento queda pendiente de verificación: se muestra en Mis eventos, no en el feed
                onEventSubmitted = {
                    navController.fromTop(entry) {
                        navigate(MainRoutes.MyEvents) { popUpTo<MainRoutes.CreateEvent> { inclusive = true } }
                    }
                },
                onPickLocation = { current -> navController.fromTop(entry) { navigate(pickLocationRoute(current, defaultCenter)) } },
                pickedLocation = picked?.let(::decodePickedLocation),
                onPickedLocationConsumed = { entry.savedStateHandle.remove<String>(PICKED_LOCATION_KEY) },
                bottomBar = { bottomBar(BottomDestination.CREATE) }
            )
        }
        composable<MainRoutes.EditEvent> { entry ->
            val eventId = entry.toRoute<MainRoutes.EditEvent>().eventId
            val picked by entry.savedStateHandle.getStateFlow<String?>(PICKED_LOCATION_KEY, null).collectAsState()
            CreateEventScreen(
                eventId = eventId,
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onEventSubmitted = { navController.fromTop(entry) { popBackStack() } },
                onPickLocation = { current -> navController.fromTop(entry) { navigate(pickLocationRoute(current, defaultCenter)) } },
                pickedLocation = picked?.let(::decodePickedLocation),
                onPickedLocationConsumed = { entry.savedStateHandle.remove<String>(PICKED_LOCATION_KEY) },
                bottomBar = { bottomBar(null) }
            )
        }
        composable<MainRoutes.PickLocation> { entry ->
            val route = entry.toRoute<MainRoutes.PickLocation>()
            PickLocationScreen(
                initial = PickedLocation(route.address, Location(route.latitude, route.longitude)),
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onConfirm = { picked ->
                    navController.fromTop(entry) {
                        previousBackStackEntry?.savedStateHandle?.set(PICKED_LOCATION_KEY, encodePickedLocation(picked))
                        popBackStack()
                    }
                }
            )
        }
        composable<MainRoutes.Map> { entry ->
            MapScreen(
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onOpenList = { navController.openTab(BottomDestination.HOME, rootRoute) },
                onOpenProfile = { navController.openTab(BottomDestination.PROFILE, rootRoute) },
                bottomBar = { bottomBar(BottomDestination.MAP) }
            )
        }
        composable<MainRoutes.MyEvents> { entry ->
            MyEventsScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onCreateEvent = { navController.openTab(BottomDestination.CREATE, rootRoute) },
                onEditEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EditEvent(eventId)) } },
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                bottomBar = { bottomBar(null) }
            )
        }
        composable<MainRoutes.Notifications> { entry ->
            NotificationsScreen(
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onOpenProfile = { navController.openTab(BottomDestination.PROFILE, rootRoute) },
                bottomBar = { bottomBar(BottomDestination.ALERTS) }
            )
        }
        composable<MainRoutes.Profile> { entry ->
            ProfileScreen(
                onEditProfile = { navController.fromTop(entry) { navigate(MainRoutes.EditProfile) } },
                onOpenStats = { navController.fromTop(entry) { navigate(MainRoutes.Stats) } },
                onOpenMyEvents = { navController.fromTop(entry) { navigate(MainRoutes.MyEvents) } },
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onOpenMyQr = { navController.fromTop(entry) { navigate(MainRoutes.CheckIn(showMyQr = true)) } },
                onOpenNotifications = { navController.openTab(BottomDestination.ALERTS, rootRoute) },
                onOpenModeration = { navController.openTab(BottomDestination.MODERATION, rootRoute) },
                onLoggedOut = { navController.backToLogin() },
                bottomBar = { bottomBar(BottomDestination.PROFILE) }
            )
        }
        composable<MainRoutes.EditProfile> { entry ->
            val picked by entry.savedStateHandle.getStateFlow<String?>(PICKED_LOCATION_KEY, null).collectAsState()
            EditProfileScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onSaved = { navController.fromTop(entry) { popBackStack() } },
                onPickLocation = { current -> navController.fromTop(entry) { navigate(pickLocationRoute(current, defaultCenter)) } },
                pickedLocation = picked?.let(::decodePickedLocation),
                onPickedLocationConsumed = { entry.savedStateHandle.remove<String>(PICKED_LOCATION_KEY) },
                onAccountDeleted = { navController.backToLogin() }
            )
        }
        composable<MainRoutes.Stats> { entry ->
            StatsScreen(
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } }
            )
        }
        composable<MainRoutes.CheckIn> { entry ->
            val route = entry.toRoute<MainRoutes.CheckIn>()
            CheckInScreen(
                eventId = route.eventId,
                showMyQr = route.showMyQr,
                onBack = { navController.fromTop(entry) { popBackStack() } },
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onOpenProfile = { navController.openTab(BottomDestination.PROFILE, rootRoute) }
            )
        }
        composable<MainRoutes.Moderation> { entry ->
            ModerationScreen(
                onOpenEvent = { eventId -> navController.fromTop(entry) { navigate(MainRoutes.EventDetail(eventId)) } },
                onOpenProfile = { navController.openTab(BottomDestination.PROFILE, rootRoute) },
                bottomBar = { bottomBar(BottomDestination.MODERATION) }
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

/** Entra a la app sacando de la pila el Login y lo que haya encima (Registro). */
private fun NavHostController.navigateAfterLogin(isModerator: Boolean) {
    navigate(if (isModerator) MainRoutes.Moderation else MainRoutes.Home) {
        popUpTo<MainRoutes.Login> { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Cambia de pestaña de la barra inferior. Las pestañas se apilan sobre [root] (Home o el panel de
 * moderación), así "atrás" desde cualquier pestaña regresa a la raíz y de ahí sale de la app.
 */
private fun NavHostController.openTab(destination: BottomDestination, root: MainRoutes) {
    val route: MainRoutes = when (destination) {
        BottomDestination.HOME -> MainRoutes.Home
        BottomDestination.MAP -> MainRoutes.Map
        BottomDestination.CREATE -> MainRoutes.CreateEvent
        BottomDestination.MODERATION -> MainRoutes.Moderation
        BottomDestination.ALERTS -> MainRoutes.Notifications
        BottomDestination.PROFILE -> MainRoutes.Profile
    }
    if (route == root) {
        popBackStack(root, inclusive = false)
        return
    }
    navigate(route) {
        popUpTo(root) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Tras cerrar sesión o eliminar la cuenta, vuelve al Login y vacía la pila. */
private fun NavHostController.backToLogin() {
    navigate(MainRoutes.Login) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun pickLocationRoute(current: PickedLocation?, defaultCenter: Location): MainRoutes.PickLocation {
    val location = current?.location ?: defaultCenter
    return MainRoutes.PickLocation(location.latitude, location.longitude, current?.address.orEmpty())
}

// El SavedStateHandle guarda tipos simples: el punto viaja como "latitud|longitud|dirección"
private fun encodePickedLocation(picked: PickedLocation): String =
    "${picked.location.latitude}|${picked.location.longitude}|${picked.address}"

private fun decodePickedLocation(encoded: String): PickedLocation? {
    val parts = encoded.split("|", limit = 3)
    val latitude = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
    val longitude = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
    return PickedLocation(parts.getOrNull(2).orEmpty(), Location(latitude, longitude))
}
