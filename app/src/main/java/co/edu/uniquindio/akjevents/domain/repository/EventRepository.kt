package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.Interest
import kotlinx.coroutines.flow.StateFlow

enum class AttendanceResult { CONFIRMED, ALREADY_CONFIRMED, FULL, CLOSED, NOT_FOUND }

sealed interface CheckInResult {
    data class Success(val event: CommunityEvent, val pointsEarned: Int) : CheckInResult
    data class AlreadyCheckedIn(val event: CommunityEvent) : CheckInResult
    data class Full(val event: CommunityEvent) : CheckInResult
    data class OwnEvent(val event: CommunityEvent) : CheckInResult
    data object InvalidCode : CheckInResult
}

/**
 * Fuente de los eventos y de la participación de los usuarios (asistencia, interés y comentarios).
 * Solo los eventos [co.edu.uniquindio.akjevents.domain.model.EventStatus.VERIFIED] son públicos:
 * el feed, la asistencia y el interés trabajan únicamente con ellos.
 * Las operaciones de gestión reciben quién las hace ([actorId]) y comprueban sus permisos.
 */
interface EventRepository {
    /** Todos los eventos, en cualquier estado. Úsese solo para el organizador o el moderador. */
    val events: StateFlow<List<CommunityEvent>>

    /** Eventos verificados, con sus conteos de asistentes e interesados al día. */
    val publicEvents: StateFlow<List<CommunityEvent>>

    val comments: StateFlow<List<Comment>>

    val attendances: StateFlow<List<Attendance>>

    val interests: StateFlow<List<Interest>>

    fun findEvent(id: String): CommunityEvent?

    fun findPublicEvent(id: String): CommunityEvent?

    /** Eventos creados por un usuario, en cualquier estado (pendientes y rechazados incluidos). */
    fun findEventsByOrganizer(organizerId: String): List<CommunityEvent>

    /** Guarda un evento nuevo. Si no está verificado, no aparece en [publicEvents]. */
    fun createEvent(event: CommunityEvent)

    /** Solo el organizador puede editarlo. El evento vuelve a revisión. */
    fun updateEvent(event: CommunityEvent, actorId: String): Boolean

    /** Lo puede borrar su organizador o un moderador. */
    fun deleteEvent(eventId: String, actorId: String): Boolean

    fun verifyEvent(eventId: String, moderatorId: String): Boolean

    /** Rechazar exige un motivo, que se le notifica al organizador. */
    fun rejectEvent(eventId: String, moderatorId: String, reason: String): Boolean

    /** Lo puede finalizar su organizador o cualquier moderador. */
    fun finishEvent(eventId: String, actorId: String): Boolean

    /** Suma un asistente si hay cupo y el usuario no había confirmado antes. */
    fun confirmAttendance(eventId: String, userId: String): AttendanceResult

    /** Devuelve false si el usuario no tenía asistencia confirmada o ya hizo check-in. */
    fun cancelAttendance(eventId: String, userId: String): Boolean

    /** Valida el código QR de un evento y registra la asistencia presencial. */
    fun checkIn(code: String, userId: String): CheckInResult

    /** Marca o desmarca "Me interesa". Devuelve el nuevo estado, o null si el evento no es público. */
    fun toggleInterest(eventId: String, userId: String): Boolean?

    /** Comentario en un evento verificado o finalizado; avisa al organizador. */
    fun addComment(eventId: String, authorId: String, text: String): Comment?

    /** Aviso del organizador a sus asistentes. Devuelve a cuántas personas les llegó. */
    fun sendNotice(eventId: String, organizerId: String, message: String): Int

    /** Borra los eventos, comentarios y participación de un usuario que elimina su cuenta. */
    fun removeUserData(userId: String)
}
