package co.edu.uniquindio.akjevents.navigation

import kotlinx.serialization.Serializable

sealed interface MainRoutes {
    @Serializable
    data object Splash : MainRoutes

    @Serializable
    data object Login : MainRoutes

    @Serializable
    data object Register : MainRoutes

    @Serializable
    data object RecoverPassword : MainRoutes

    @Serializable
    data object Home : MainRoutes

    @Serializable
    data class EventDetail(val eventId: String) : MainRoutes

    @Serializable
    data object CreateEvent : MainRoutes
}
