package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import kotlinx.coroutines.flow.StateFlow

/** Notificaciones dentro de la app; se consultan al abrir el apartado de Alertas. */
interface NotificationRepository {
    /** Todas las notificaciones; cada pantalla filtra las del usuario de la sesión. */
    val notifications: StateFlow<List<AppNotification>>

    fun notify(userId: String, type: NotificationType, title: String, message: String, eventId: String? = null)

    fun setRead(notificationId: String, isRead: Boolean)

    fun markAllAsRead(userId: String)

    fun deleteForUser(userId: String)
}
