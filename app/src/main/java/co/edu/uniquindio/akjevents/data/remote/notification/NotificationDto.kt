package co.edu.uniquindio.akjevents.data.remote.notification

import androidx.annotation.Keep
import co.edu.uniquindio.akjevents.data.remote.toLocalDateTime
import co.edu.uniquindio.akjevents.data.remote.toTimestamp
import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import com.google.firebase.Timestamp
import java.time.LocalDateTime

/**
 * Notificación en la colección `notifications`. Guarda el tipo y los datos, no frases: cada persona
 * la lee en su idioma. El campo de leída se llama `read` (Firestore no mapea bien `isRead`).
 */
@Keep
data class NotificationDto(
    val userId: String = "",
    val type: String = "",
    val createdAt: Timestamp? = null,
    val eventId: String? = null,
    val read: Boolean = false,
    val eventTitle: String? = null,
    val actorName: String? = null,
    val detail: String? = null,
    val badge: String? = null,
    val level: String? = null
) {
    /** Devuelve null si el tipo no existe (por ejemplo, de una versión más nueva de la app). */
    fun toDomain(id: String): AppNotification? {
        val notificationType = runCatching { NotificationType.valueOf(type) }.getOrNull() ?: return null
        return AppNotification(
            id = id,
            userId = userId,
            type = notificationType,
            createdAt = createdAt?.toLocalDateTime() ?: LocalDateTime.now(),
            eventId = eventId,
            isRead = read,
            eventTitle = eventTitle,
            actorName = actorName,
            detail = detail,
            badge = badge?.let { runCatching { Badge.valueOf(it) }.getOrNull() },
            level = level?.let { runCatching { UserLevel.valueOf(it) }.getOrNull() }
        )
    }

    companion object {
        fun fromDomain(notification: AppNotification) = NotificationDto(
            userId = notification.userId,
            type = notification.type.name,
            createdAt = notification.createdAt.toTimestamp(),
            eventId = notification.eventId,
            read = notification.isRead,
            eventTitle = notification.eventTitle,
            actorName = notification.actorName,
            detail = notification.detail,
            badge = notification.badge?.name,
            level = notification.level?.name
        )
    }
}
