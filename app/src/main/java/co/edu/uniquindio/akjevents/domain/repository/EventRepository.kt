package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import kotlinx.coroutines.flow.StateFlow

enum class AttendanceResult { CONFIRMED, ALREADY_CONFIRMED, FULL, NOT_FOUND }

/**
 * Fuente de los eventos y de la participación del usuario de la sesión.
 * Solo expone eventos públicos ([co.edu.uniquindio.akjevents.domain.model.EventStatus.VERIFIED]).
 */
interface EventRepository {
    /** Eventos verificados, con sus conteos de asistentes e interesados al día. */
    val publicEvents: StateFlow<List<CommunityEvent>>

    /** Eventos a los que el usuario de la sesión confirmó asistencia. */
    val attendingEventIds: StateFlow<Set<String>>

    /** Eventos que el usuario de la sesión marcó con "Me interesa". */
    val interestedEventIds: StateFlow<Set<String>>

    fun findPublicEvent(id: String): CommunityEvent?

    /** Eventos creados por un usuario, en cualquier estado (pendientes y rechazados incluidos). */
    fun findEventsByOrganizer(organizerId: String): List<CommunityEvent>

    /** Guarda un evento nuevo. Si no está verificado, no aparece en [publicEvents]. */
    fun createEvent(event: CommunityEvent)

    /** Suma un asistente si hay cupo y el usuario no había confirmado antes. */
    fun confirmAttendance(eventId: String): AttendanceResult

    /** Devuelve false si el usuario no tenía asistencia confirmada en ese evento. */
    fun cancelAttendance(eventId: String): Boolean

    /** Marca o desmarca "Me interesa". Devuelve el nuevo estado, o null si el evento no es público. */
    fun toggleInterest(eventId: String): Boolean?
}
