package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

/** Grupos de las pestañas del apartado de notificaciones (mockup 14). */
enum class NotificationGroup(val label: String) {
    EVENTS("Eventos"),
    MODERATION("Moderación"),
    ACHIEVEMENTS("Logros")
}

enum class NotificationType(val group: NotificationGroup) {
    NEW_COMMENT(NotificationGroup.EVENTS),
    ORGANIZER_NOTICE(NotificationGroup.EVENTS),
    EVENT_FINISHED(NotificationGroup.EVENTS),
    REMINDER(NotificationGroup.EVENTS),
    EVENT_VERIFIED(NotificationGroup.MODERATION),
    EVENT_REJECTED(NotificationGroup.MODERATION),
    BADGE_UNLOCKED(NotificationGroup.ACHIEVEMENTS),
    LEVEL_UP(NotificationGroup.ACHIEVEMENTS)
}

/** Notificación dentro de la app; se llama así para no confundirse con android.app.Notification. */
data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val createdAt: LocalDateTime,
    val eventId: String? = null,
    val isRead: Boolean = false
)
