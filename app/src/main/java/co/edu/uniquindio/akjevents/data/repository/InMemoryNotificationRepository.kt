package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleParticipation
import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID

/** Guarda las notificaciones en memoria: duran mientras la app esté abierta. */
class InMemoryNotificationRepository(
    initialNotifications: List<AppNotification> = SampleParticipation.notifications,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : NotificationRepository {

    private val _notifications = MutableStateFlow(initialNotifications.sortedByDescending { it.createdAt })
    override val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    override fun notify(userId: String, type: NotificationType, title: String, message: String, eventId: String?) {
        val notification = AppNotification(
            id = "notificacion-${UUID.randomUUID()}",
            userId = userId,
            type = type,
            title = title,
            message = message,
            createdAt = now(),
            eventId = eventId
        )
        // La más reciente va primero
        _notifications.update { listOf(notification) + it }
    }

    override fun setRead(notificationId: String, isRead: Boolean) = _notifications.update { list ->
        list.map { if (it.id == notificationId) it.copy(isRead = isRead) else it }
    }

    override fun markAllAsRead(userId: String) = _notifications.update { list ->
        list.map { if (it.userId == userId) it.copy(isRead = true) else it }
    }

    override fun deleteForUser(userId: String) = _notifications.update { list -> list.filterNot { it.userId == userId } }

    companion object {
        /** Instancia compartida por las pantallas mientras la app esté abierta (aún no hay inyección de dependencias). */
        val shared: NotificationRepository by lazy { InMemoryNotificationRepository() }
    }
}
