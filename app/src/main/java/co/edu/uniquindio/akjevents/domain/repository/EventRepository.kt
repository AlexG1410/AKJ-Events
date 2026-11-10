package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import kotlinx.coroutines.flow.StateFlow

enum class AttendanceResult { CONFIRMED, ALREADY_CONFIRMED, FULL, CLOSED, NOT_FOUND, OWN_EVENT }

/** Resultado de guardar un check-in; los puntos los suma el caso de uso. */
enum class CheckInRegistration { REGISTERED, ALREADY_CHECKED_IN, FULL, NOT_FOUND, OWN_EVENT }

/**
 * Fuente de los eventos y de la participación de los usuarios (asistencia, interés y comentarios).
 * Solo guarda y consulta datos: los permisos, los puntos, las insignias y las notificaciones
 * los aplican los casos de uso de `domain/usecase`.
 * Solo los eventos [EventStatus.VERIFIED] son públicos: el feed, la asistencia y el interés trabajan únicamente con ellos.
 */
interface EventRepository {
    /**
     * Los eventos que la sesión puede ver, en cualquier estado: para un moderador, todos; para un usuario, los
     * verificados y finalizados más los propios. Úsese solo para el organizador o el moderador.
     */
    val events: StateFlow<List<CommunityEvent>>

    /** Eventos verificados, con sus conteos de asistentes e interesados al día. */
    val publicEvents: StateFlow<List<CommunityEvent>>

    val comments: StateFlow<List<Comment>>

    val attendances: StateFlow<List<Attendance>>

    val interests: StateFlow<List<Interest>>

    suspend fun findEvent(id: String): CommunityEvent?

    suspend fun findPublicEvent(id: String): CommunityEvent?

    /** Evento público cuyo QR de asistencia tiene ese código. */
    suspend fun findPublicEventByCheckInCode(code: String): CommunityEvent?

    /** Eventos creados por un usuario, en cualquier estado (pendientes y rechazados incluidos). */
    suspend fun findEventsByOrganizer(organizerId: String): List<CommunityEvent>

    /** Guarda un evento nuevo. Si no está verificado, no aparece en [publicEvents]. */
    suspend fun createEvent(event: CommunityEvent)

    /**
     * Reemplaza los datos del evento. Conserva el organizador, los conteos y la fecha de creación.
     * Devuelve el evento guardado, o null si no existe.
     */
    suspend fun updateEvent(event: CommunityEvent): CommunityEvent?

    /** Cambia el estado del evento; [rejectionReason] solo aplica a [EventStatus.REJECTED]. */
    suspend fun updateStatus(eventId: String, status: EventStatus, rejectionReason: String? = null): CommunityEvent?

    /**
     * Registra que el evento ya dio los puntos de su verificación. Devuelve false si ya los había dado,
     * para que editarlo y volverlo a aprobar no los sume otra vez.
     */
    suspend fun registerVerificationReward(eventId: String): Boolean

    /** Borra el evento con sus comentarios, asistencias e intereses. */
    suspend fun deleteEvent(eventId: String): Boolean

    /** Suma un asistente si hay cupo y el usuario no había confirmado antes; excluye al organizador. */
    suspend fun confirmAttendance(eventId: String, userId: String): AttendanceResult

    /** Devuelve false si el usuario no tenía asistencia confirmada o ya hizo check-in. */
    suspend fun cancelAttendance(eventId: String, userId: String): Boolean

    /** Registra asistencia presencial de participantes, nunca del organizador; ocupa un cupo si hace falta. */
    suspend fun registerCheckIn(eventId: String, userId: String): CheckInRegistration

    /** Marca o desmarca "Me interesa". Devuelve el nuevo estado, o null si el evento no es público. */
    suspend fun toggleInterest(eventId: String, userId: String): Boolean?

    /** Guarda un comentario y le asigna su id y su fecha. */
    suspend fun addComment(eventId: String, authorId: String, text: String): Comment

    /** Borra los eventos, comentarios y participación de un usuario que elimina su cuenta. */
    suspend fun removeUserData(userId: String)
}
