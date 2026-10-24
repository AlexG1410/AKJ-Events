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
    data object Recommendations : MainRoutes

    @Serializable
    data class EventDetail(val eventId: String) : MainRoutes

    @Serializable
    data object CreateEvent : MainRoutes

    @Serializable
    data class EditEvent(val eventId: String) : MainRoutes

    /** Elegir un punto en el mapa; devuelve el resultado a la pantalla anterior. */
    @Serializable
    data class PickLocation(val latitude: Double, val longitude: Double, val address: String = "") : MainRoutes

    @Serializable
    data object Map : MainRoutes

    @Serializable
    data object MyEvents : MainRoutes

    @Serializable
    data object Notifications : MainRoutes

    @Serializable
    data object Profile : MainRoutes

    @Serializable
    data object EditProfile : MainRoutes

    @Serializable
    data object Stats : MainRoutes

    /** Check-in con QR; [eventId] preselecciona el evento a validar. */
    @Serializable
    data class CheckIn(val eventId: String? = null, val showMyQr: Boolean = false) : MainRoutes

    @Serializable
    data object Moderation : MainRoutes
}
