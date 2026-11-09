package co.edu.uniquindio.akjevents.data.remote.event

import androidx.annotation.Keep
import co.edu.uniquindio.akjevents.data.remote.toLocalDateTime
import co.edu.uniquindio.akjevents.data.remote.toTimestamp
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.Location
import com.google.firebase.Timestamp
import java.time.LocalDateTime

/**
 * Evento en la colección `events` (guía 19: todos los campos con valor por defecto para que Firestore
 * pueda crearlo). El id del documento es el id del evento.
 */
@Keep
data class EventDto(
    val organizerId: String = "",
    val title: String = "",
    val category: String = EventCategory.SOCIAL.name,
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrls: List<String> = emptyList(),
    val startsAt: Timestamp? = null,
    val endsAt: Timestamp? = null,
    val capacity: Int? = null,
    val attendanceCount: Int = 0,
    val interestCount: Int = 0,
    val status: String = EventStatus.PENDING.name,
    val rejectionReason: String? = null,
    val createdAt: Timestamp? = null,
    /** Código del QR de asistencia; se guarda para poder buscar el evento al hacer check-in. */
    val checkInCode: String = "",
    /** Si el evento ya dio los puntos de su verificación (no se repiten al editarlo y aprobarlo otra vez). */
    val verificationRewarded: Boolean = false
) {
    /** Lanza una excepción si el documento no cumple las reglas de [CommunityEvent]. */
    fun toDomain(id: String): CommunityEvent {
        val start = startsAt?.toLocalDateTime() ?: error("El evento $id no tiene fecha de inicio")
        return CommunityEvent(
            id = id,
            organizerId = organizerId,
            title = title,
            category = runCatching { EventCategory.valueOf(category) }.getOrDefault(EventCategory.SOCIAL),
            description = description,
            address = address,
            location = Location(latitude, longitude),
            imageUrls = imageUrls,
            startsAt = start,
            endsAt = endsAt?.toLocalDateTime() ?: error("El evento $id no tiene fecha de fin"),
            capacity = capacity,
            attendanceCount = attendanceCount,
            interestCount = interestCount,
            status = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.PENDING),
            rejectionReason = rejectionReason,
            createdAt = createdAt?.toLocalDateTime() ?: start.minusDays(7)
        )
    }

    companion object {
        fun fromDomain(event: CommunityEvent, verificationRewarded: Boolean = false) = EventDto(
            organizerId = event.organizerId,
            title = event.title,
            category = event.category.name,
            description = event.description,
            address = event.address,
            latitude = event.location.latitude,
            longitude = event.location.longitude,
            imageUrls = event.imageUrls,
            startsAt = event.startsAt.toTimestamp(),
            endsAt = event.endsAt.toTimestamp(),
            capacity = event.capacity,
            attendanceCount = event.attendanceCount,
            interestCount = event.interestCount,
            status = event.status.name,
            rejectionReason = event.rejectionReason,
            createdAt = event.createdAt.toTimestamp(),
            checkInCode = event.checkInCode,
            verificationRewarded = verificationRewarded
        )
    }
}

/** Comentario en la colección `comments`; el id del documento es el del comentario. */
@Keep
data class CommentDto(
    val eventId: String = "",
    val authorId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
) {
    fun toDomain(id: String) = Comment(id, eventId, authorId, text, createdAt?.toLocalDateTime() ?: LocalDateTime.now())

    companion object {
        fun fromDomain(comment: Comment) = CommentDto(comment.eventId, comment.authorId, comment.text, comment.createdAt.toTimestamp())
    }
}

/** Asistencia en la colección `attendances`; el id del documento es `eventId_userId`, así no se repite. */
@Keep
data class AttendanceDto(
    val eventId: String = "",
    val userId: String = "",
    val checkedIn: Boolean = false
) {
    fun toDomain() = Attendance(eventId, userId, checkedIn)
}

/** "Me interesa" en la colección `interests`; el id del documento es `eventId_userId`. */
@Keep
data class InterestDto(
    val eventId: String = "",
    val userId: String = ""
) {
    fun toDomain() = Interest(eventId, userId)
}

/** Id del documento de una asistencia o un interés: uno por usuario y evento. */
fun participationId(eventId: String, userId: String) = "${eventId}_$userId"
