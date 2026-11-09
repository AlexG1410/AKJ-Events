package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

/** Grupos de las pestañas del apartado de notificaciones (mockup 14). */
enum class NotificationGroup {
    EVENTS,
    MODERATION,
    ACHIEVEMENTS
}

enum class NotificationType(val group: NotificationGroup) {
    NEW_COMMENT(NotificationGroup.EVENTS),
    ORGANIZER_NOTICE(NotificationGroup.EVENTS),
    EVENT_FINISHED(NotificationGroup.EVENTS),
    /** Un moderador finalizó el evento; se le avisa al organizador. */
    EVENT_FINISHED_BY_MODERATOR(NotificationGroup.EVENTS),
    REMINDER(NotificationGroup.EVENTS),
    EVENT_VERIFIED(NotificationGroup.MODERATION),
    EVENT_REJECTED(NotificationGroup.MODERATION),
    BADGE_UNLOCKED(NotificationGroup.ACHIEVEMENTS),
    LEVEL_UP(NotificationGroup.ACHIEVEMENTS)
}

/**
 * Notificación dentro de la app; se llama así para no confundirse con android.app.Notification.
 * No guarda frases: guarda el tipo y los datos, y la pantalla arma el título y el mensaje en el
 * idioma de quien la lee (español o inglés).
 */
data class AppNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
    val eventId: String? = null,
    val isRead: Boolean = false,
    /** Título del evento relacionado. */
    val eventTitle: String? = null,
    /** Quién hizo la acción, por ejemplo el autor de un comentario. */
    val actorName: String? = null,
    /** Texto escrito por una persona: el comentario, el motivo del rechazo o el aviso del organizador. */
    val detail: String? = null,
    val badge: Badge? = null,
    val level: UserLevel? = null
)
