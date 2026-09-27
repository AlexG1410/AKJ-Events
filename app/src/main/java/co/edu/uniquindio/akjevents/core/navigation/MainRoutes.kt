package co.edu.uniquindio.akjevents.core.navigation

import kotlinx.serialization.Serializable

sealed interface MainRoutes {
    @Serializable
    data object Home : MainRoutes

    @Serializable
    data class EventDetail(val eventId: String) : MainRoutes
}
