package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.data.demo.SampleParticipation
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.CheckInRegistration
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda los eventos y la participación en memoria: los cambios duran mientras la app esté abierta.
 * Las reglas de la comunidad (permisos, puntos, insignias y notificaciones) están en los casos de uso.
 * Hilt crea una sola instancia con los datos de muestra; las pruebas usan el constructor principal.
 */
@Singleton
class InMemoryEventRepository(
    initialEvents: List<CommunityEvent>,
    initialComments: List<Comment> = SampleParticipation.comments,
    initialAttendances: List<Attendance> = SampleParticipation.attendances,
    initialInterests: List<Interest> = SampleParticipation.interests,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : EventRepository {

    @Inject
    constructor() : this(SampleEvents.all)

    private val lock = Any()

    // Los eventos de muestra ya aprobados parten con su recompensa de verificación contabilizada
    private val verificationRewardedIds = initialEvents
        .filter { it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED }
        .map { it.id }.toMutableSet()

    // Todos los eventos, incluidos los que no son públicos (pendientes, rechazados...)
    private val _events = MutableStateFlow(initialEvents)
    override val events: StateFlow<List<CommunityEvent>> = _events.asStateFlow()

    private val _publicEvents = MutableStateFlow(initialEvents.filter(::isPublic))
    override val publicEvents: StateFlow<List<CommunityEvent>> = _publicEvents.asStateFlow()

    private val _comments = MutableStateFlow(initialComments)
    override val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _attendances = MutableStateFlow(initialAttendances)
    override val attendances: StateFlow<List<Attendance>> = _attendances.asStateFlow()

    private val _interests = MutableStateFlow(initialInterests)
    override val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    override fun findEvent(id: String): CommunityEvent? = _events.value.find { it.id == id }

    override fun findPublicEvent(id: String): CommunityEvent? = _publicEvents.value.find { it.id == id }

    override fun findPublicEventByCheckInCode(code: String): CommunityEvent? =
        _publicEvents.value.find { it.checkInCode == code }

    override fun findEventsByOrganizer(organizerId: String): List<CommunityEvent> =
        _events.value.filter { it.organizerId == organizerId }

    override fun createEvent(event: CommunityEvent) = synchronized(lock) {
        require(_events.value.none { it.id == event.id }) { "Ya existe un evento con el id ${event.id}" }
        setEvents(_events.value + event)
    }

    override fun updateEvent(event: CommunityEvent): CommunityEvent? = synchronized(lock) {
        val existing = findEvent(event.id) ?: return null
        // Los conteos y el organizador no se pueden alterar desde el formulario
        val updated = event.copy(
            organizerId = existing.organizerId,
            attendanceCount = existing.attendanceCount,
            interestCount = existing.interestCount,
            createdAt = existing.createdAt
        )
        replace(updated)
        updated
    }

    override fun updateStatus(eventId: String, status: EventStatus, rejectionReason: String?): CommunityEvent? =
        synchronized(lock) {
            val event = findEvent(eventId) ?: return null
            val reason = if (status == EventStatus.REJECTED) rejectionReason else null
            event.copy(status = status, rejectionReason = reason).also(::replace)
        }

    override fun registerVerificationReward(eventId: String): Boolean = synchronized(lock) {
        verificationRewardedIds.add(eventId)
    }

    override fun deleteEvent(eventId: String): Boolean = synchronized(lock) {
        if (findEvent(eventId) == null) return false
        setEvents(_events.value.filterNot { it.id == eventId })
        _comments.update { list -> list.filterNot { it.eventId == eventId } }
        _attendances.update { list -> list.filterNot { it.eventId == eventId } }
        _interests.update { list -> list.filterNot { it.eventId == eventId } }
        true
    }

    override fun confirmAttendance(eventId: String, userId: String): AttendanceResult = synchronized(lock) {
        val event = findPublicEvent(eventId)
            ?: return if (findEvent(eventId)?.status == EventStatus.FINISHED) AttendanceResult.CLOSED else AttendanceResult.NOT_FOUND
        when {
            isAttending(eventId, userId) -> AttendanceResult.ALREADY_CONFIRMED
            event.isFull -> AttendanceResult.FULL
            else -> {
                replace(event.copy(attendanceCount = event.attendanceCount + 1))
                _attendances.update { it + Attendance(eventId, userId) }
                AttendanceResult.CONFIRMED
            }
        }
    }

    override fun cancelAttendance(eventId: String, userId: String): Boolean = synchronized(lock) {
        val event = findPublicEvent(eventId)
        val attendance = _attendances.value.find { it.eventId == eventId && it.userId == userId }
        if (event == null || attendance == null || attendance.checkedIn) return false
        replace(event.copy(attendanceCount = (event.attendanceCount - 1).coerceAtLeast(0)))
        _attendances.update { it - attendance }
        true
    }

    override fun registerCheckIn(eventId: String, userId: String): CheckInRegistration = synchronized(lock) {
        val event = findPublicEvent(eventId) ?: return CheckInRegistration.NOT_FOUND
        val attendance = _attendances.value.find { it.eventId == eventId && it.userId == userId }
        when {
            attendance?.checkedIn == true -> return CheckInRegistration.ALREADY_CHECKED_IN
            attendance != null -> _attendances.update { list -> list.map { if (it == attendance) it.copy(checkedIn = true) else it } }
            event.isFull -> return CheckInRegistration.FULL
            else -> {
                replace(event.copy(attendanceCount = event.attendanceCount + 1))
                _attendances.update { it + Attendance(eventId, userId, checkedIn = true) }
            }
        }
        CheckInRegistration.REGISTERED
    }

    override fun toggleInterest(eventId: String, userId: String): Boolean? = synchronized(lock) {
        val event = findPublicEvent(eventId) ?: return null
        val interest = Interest(eventId, userId)
        val interested = interest !in _interests.value
        replace(event.copy(interestCount = (event.interestCount + if (interested) 1 else -1).coerceAtLeast(0)))
        _interests.update { if (interested) it + interest else it - interest }
        interested
    }

    override fun addComment(eventId: String, authorId: String, text: String): Comment {
        val comment = Comment("comentario-${UUID.randomUUID()}", eventId, authorId, text, now())
        _comments.update { it + comment }
        return comment
    }

    override fun removeUserData(userId: String) = synchronized(lock) {
        val ownEventIds = findEventsByOrganizer(userId).map { it.id }.toSet()
        val leftAttendances = _attendances.value.filter { it.userId == userId }.map { it.eventId }
        val leftInterests = _interests.value.filter { it.userId == userId }.map { it.eventId }
        // Sus asistencias e intereses dejan de contar en los eventos de los demás
        val remaining = _events.value.filterNot { it.id in ownEventIds }.map { event ->
            event.copy(
                attendanceCount = (event.attendanceCount - leftAttendances.count { it == event.id }).coerceAtLeast(0),
                interestCount = (event.interestCount - leftInterests.count { it == event.id }).coerceAtLeast(0)
            )
        }
        setEvents(remaining)
        _comments.update { list -> list.filterNot { it.authorId == userId || it.eventId in ownEventIds } }
        _attendances.update { list -> list.filterNot { it.userId == userId || it.eventId in ownEventIds } }
        _interests.update { list -> list.filterNot { it.userId == userId || it.eventId in ownEventIds } }
    }

    private val CommunityEvent.isFull: Boolean get() = capacity != null && attendanceCount >= capacity

    private fun isAttending(eventId: String, userId: String) = _attendances.value.any { it.eventId == eventId && it.userId == userId }

    private fun replace(updated: CommunityEvent) = setEvents(_events.value.map { if (it.id == updated.id) updated else it })

    private fun setEvents(events: List<CommunityEvent>) {
        _events.value = events
        _publicEvents.value = events.filter(::isPublic)
    }

    private fun isPublic(event: CommunityEvent) = event.status == EventStatus.VERIFIED
}
