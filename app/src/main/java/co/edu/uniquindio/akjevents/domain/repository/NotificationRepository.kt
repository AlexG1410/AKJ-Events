package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import kotlinx.coroutines.flow.StateFlow

/** Notificaciones dentro de la app; se consultan al abrir el apartado de Alertas. */
interface NotificationRepository {
    /** Todas las notificaciones; cada pantalla filtra las del usuario de la sesión. */
    val notifications: StateFlow<List<AppNotification>>

    suspend fun notify(userId: String, type: NotificationType, title: String, message: String, eventId: String? = null)

    suspend fun setRead(notificationId: String, isRead: Boolean)

    suspend fun markAllAsRead(userId: String)

    suspend fun deleteForUser(userId: String)
}
