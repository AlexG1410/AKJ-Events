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
import javax.inject.Inject
import javax.inject.Singleton

/** Guarda las notificaciones en memoria: duran mientras la app esté abierta. */
@Singleton
class InMemoryNotificationRepository(
    initialNotifications: List<AppNotification>,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : NotificationRepository {

    @Inject
    constructor() : this(SampleParticipation.notifications)

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
}
