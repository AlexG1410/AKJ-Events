package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import kotlinx.coroutines.flow.StateFlow

/** Notificaciones dentro de la app; se consultan al abrir el apartado de Alertas. */
interface NotificationRepository {
    /** Todas las notificaciones; cada pantalla filtra las del usuario de la sesión. */
    val notifications: StateFlow<List<AppNotification>>

    /** Guarda una notificación; los textos se arman al mostrarla, según el idioma del usuario. */
    suspend fun notify(
        userId: String,
        type: NotificationType,
        eventId: String? = null,
        eventTitle: String? = null,
        actorName: String? = null,
        detail: String? = null,
        badge: Badge? = null,
        level: UserLevel? = null
    )

    suspend fun setRead(notificationId: String, isRead: Boolean)

    suspend fun markAllAsRead(userId: String)

    suspend fun deleteForUser(userId: String)
}
