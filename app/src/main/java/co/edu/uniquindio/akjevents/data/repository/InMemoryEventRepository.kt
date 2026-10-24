package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.data.demo.SampleParticipation
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.CheckInResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

/**
 * Guarda los eventos y la participación en memoria: los cambios duran mientras la app esté abierta.
 * Además de guardar, aplica las reglas de la comunidad: puntos, insignias y notificaciones.
 */
class InMemoryEventRepository(
    initialEvents: List<CommunityEvent> = SampleEvents.all,
    initialComments: List<Comment> = SampleParticipation.comments,
    initialAttendances: List<Attendance> = SampleParticipation.attendances,
    initialInterests: List<Interest> = SampleParticipation.interests,
    private val notifications: NotificationRepository = InMemoryNotificationRepository(emptyList()),
    private val users: UserRepository = InMemoryUserRepository(notifications = notifications),
    private val now: () -> LocalDateTime = LocalDateTime::now
) : EventRepository {

    private val lock = Any()

    // Una aprobación solo da puntos una vez por evento, aunque se edite y vuelva a revisión.
    // Los eventos de muestra ya aprobados parten con esa recompensa contabilizada.
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

    override fun findEventsByOrganizer(organizerId: String): List<CommunityEvent> =
        _events.value.filter { it.organizerId == organizerId }

    override fun createEvent(event: CommunityEvent) {
        synchronized(lock) {
            require(_events.value.none { it.id == event.id }) { "Ya existe un evento con el id ${event.id}" }
            setEvents(_events.value + event)
        }
        users.addPoints(event.organizerId, ReputationPoints.CREATE_EVENT, "Creaste «${event.title}»")
        if (findEventsByOrganizer(event.organizerId).size == 1) users.awardBadge(event.organizerId, Badge.FIRST_EVENT)
    }

    override fun updateEvent(event: CommunityEvent, actorId: String): Boolean = synchronized(lock) {
        val existing = findEvent(event.id) ?: return false
        if (existing.organizerId != actorId || existing.status == EventStatus.FINISHED) return false
        if (event.capacity != null && event.capacity < existing.attendanceCount) return false
        // Los cambios se revisan de nuevo; los conteos y el organizador no se pueden alterar
        val updated = event.copy(
            organizerId = existing.organizerId,
            attendanceCount = existing.attendanceCount,
            interestCount = existing.interestCount,
            createdAt = existing.createdAt,
            status = EventStatus.PENDING,
            rejectionReason = null
        )
        replace(updated)
        true
    }

    override fun deleteEvent(eventId: String, actorId: String): Boolean = synchronized(lock) {
        val event = findEvent(eventId) ?: return false
        if (event.organizerId != actorId && !isModerator(actorId)) return false
        setEvents(_events.value.filterNot { it.id == eventId })
        _comments.update { list -> list.filterNot { it.eventId == eventId } }
        _attendances.update { list -> list.filterNot { it.eventId == eventId } }
        _interests.update { list -> list.filterNot { it.eventId == eventId } }
        true
    }

    override fun verifyEvent(eventId: String, moderatorId: String): Boolean {
        val (event, shouldReward) = synchronized(lock) {
            val event = findEvent(eventId)
            if (event == null || event.status != EventStatus.PENDING || !isModerator(moderatorId)) return false
            event.copy(status = EventStatus.VERIFIED, rejectionReason = null).also(::replace) to
                verificationRewardedIds.add(event.id)
        }
        if (shouldReward) {
            users.addPoints(event.organizerId, ReputationPoints.EVENT_VERIFIED, "«${event.title}» fue verificado")
        }
        notifications.notify(
            userId = event.organizerId,
            type = NotificationType.EVENT_VERIFIED,
            title = "¡Tu evento ha sido aprobado y publicado!",
            message = "«${event.title}» superó la revisión y ya es visible para la comunidad.",
            eventId = event.id
        )
        val verifiedCount = findEventsByOrganizer(event.organizerId).count {
            it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED
        }
        if (verifiedCount >= ReputationPoints.VERIFIED_FOR_BADGE) users.awardBadge(event.organizerId, Badge.TEN_VERIFIED)
        return true
    }

    override fun rejectEvent(eventId: String, moderatorId: String, reason: String): Boolean {
        val event = synchronized(lock) {
            val event = findEvent(eventId)
            if (event == null || event.status != EventStatus.PENDING || reason.isBlank() || !isModerator(moderatorId)) return false
            event.copy(status = EventStatus.REJECTED, rejectionReason = reason.trim()).also(::replace)
        }
        notifications.notify(
            userId = event.organizerId,
            type = NotificationType.EVENT_REJECTED,
            title = "Revisión requerida: ${event.title}",
            message = "Motivo: ${event.rejectionReason}",
            eventId = event.id
        )
        return true
    }

    override fun finishEvent(eventId: String, actorId: String): Boolean {
        val (event, wasPublic) = synchronized(lock) {
            val event = findEvent(eventId) ?: return false
            val canFinish = event.organizerId == actorId || isModerator(actorId)
            if (!canFinish || event.status == EventStatus.FINISHED) return false
            event.copy(status = EventStatus.FINISHED).also(::replace) to (event.status == EventStatus.VERIFIED)
        }
        if (wasPublic) {
            users.addPoints(event.organizerId, ReputationPoints.EVENT_FINISHED, "«${event.title}» finalizó con éxito")
            _attendances.value.filter { it.eventId == eventId && it.userId != actorId }.forEach {
                notifications.notify(it.userId, NotificationType.EVENT_FINISHED, "Evento finalizado", "«${event.title}» fue marcado como finalizado. ¡Gracias por participar!", eventId)
            }
        }
        if (actorId != event.organizerId) {
            notifications.notify(
                userId = event.organizerId,
                type = NotificationType.EVENT_FINISHED,
                title = "Un moderador finalizó tu evento",
                message = "«${event.title}» quedó marcado como finalizado y ya no recibe confirmaciones.",
                eventId = eventId
            )
        }
        return true
    }

    override fun confirmAttendance(eventId: String, userId: String): AttendanceResult = synchronized(lock) {
        val event = findPublicEvent(eventId)
            ?: return if (findEvent(eventId)?.status == EventStatus.FINISHED) AttendanceResult.CLOSED else AttendanceResult.NOT_FOUND
        when {
            isAttending(eventId, userId) -> AttendanceResult.ALREADY_CONFIRMED
            event.capacity != null && event.attendanceCount >= event.capacity -> AttendanceResult.FULL
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

    override fun checkIn(code: String, userId: String): CheckInResult {
        val result = synchronized(lock) {
            val normalized = code.trim().uppercase()
            val event = _publicEvents.value.find { it.checkInCode == normalized } ?: return CheckInResult.InvalidCode
            if (event.organizerId == userId) return CheckInResult.OwnEvent(event)
            val attendance = _attendances.value.find { it.eventId == event.id && it.userId == userId }
            when {
                attendance?.checkedIn == true -> return CheckInResult.AlreadyCheckedIn(event)
                attendance != null -> _attendances.update { list -> list.map { if (it == attendance) it.copy(checkedIn = true) else it } }
                // Quien llega sin haber confirmado ocupa un cupo, si queda alguno
                event.capacity != null && event.attendanceCount >= event.capacity -> return CheckInResult.Full(event)
                else -> {
                    replace(event.copy(attendanceCount = event.attendanceCount + 1))
                    _attendances.update { it + Attendance(event.id, userId, checkedIn = true) }
                }
            }
            CheckInResult.Success(findEvent(event.id)!!, ReputationPoints.CHECK_IN)
        }
        users.addPoints(userId, ReputationPoints.CHECK_IN, "Asistencia confirmada con QR en «${result.event.title}»")
        if (_attendances.value.count { it.userId == userId && it.checkedIn } == 1) users.awardBadge(userId, Badge.FIRST_CHECK_IN)
        return result
    }

    override fun toggleInterest(eventId: String, userId: String): Boolean? {
        val (event, interested) = synchronized(lock) {
            val event = findPublicEvent(eventId) ?: return null
            val interest = Interest(eventId, userId)
            val interested = interest !in _interests.value
            val updated = event.copy(interestCount = (event.interestCount + if (interested) 1 else -1).coerceAtLeast(0))
            replace(updated)
            _interests.update { if (interested) it + interest else it - interest }
            updated to interested
        }
        // Votar por un evento propio no da puntos
        if (event.organizerId != userId) {
            val points = if (interested) ReputationPoints.INTEREST_RECEIVED else -ReputationPoints.INTEREST_RECEIVED
            val reason = if (interested) "Recibiste un «Me interesa» en «${event.title}»" else "Retiraron un «Me interesa» en «${event.title}»"
            users.addPoints(event.organizerId, points, reason)
        }
        if (interested && isMostRelevantOfItsMonth(event)) users.awardBadge(event.organizerId, Badge.FEATURED_OF_MONTH)
        return interested
    }

    override fun addComment(eventId: String, authorId: String, text: String): Comment? {
        val (event, comment) = synchronized(lock) {
            val event = findEvent(eventId)
            val canComment = event != null && (event.status == EventStatus.VERIFIED || event.status == EventStatus.FINISHED)
            if (!canComment || text.isBlank() || users.findById(authorId) == null) return null
            val comment = Comment("comentario-${UUID.randomUUID()}", eventId, authorId, text.trim(), now())
            _comments.update { it + comment }
            event to comment
        }
        users.addPoints(authorId, ReputationPoints.COMMENT, "Comentaste en «${event.title}»")
        if (_comments.value.count { it.authorId == authorId } == 1) users.awardBadge(authorId, Badge.FIRST_COMMENT)
        // El autor del evento recibe la notificación, salvo que el comentario sea suyo
        if (authorId != event.organizerId) {
            val author = users.findById(authorId)?.name ?: "Alguien"
            notifications.notify(
                userId = event.organizerId,
                type = NotificationType.NEW_COMMENT,
                title = "Nuevo comentario en tu evento",
                message = "$author comentó en «${event.title}»: «${comment.text}»",
                eventId = eventId
            )
        }
        return comment
    }

    override fun sendNotice(eventId: String, organizerId: String, message: String): Int {
        val event = findEvent(eventId)
        if (event == null || event.organizerId != organizerId || message.isBlank()) return 0
        val recipients = _attendances.value.filter { it.eventId == eventId && it.userId != organizerId }.map { it.userId }.distinct()
        recipients.forEach {
            notifications.notify(it, NotificationType.ORGANIZER_NOTICE, "Aviso del organizador: ${event.title}", message.trim(), eventId)
        }
        return recipients.size
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

    private fun isAttending(eventId: String, userId: String) = _attendances.value.any { it.eventId == eventId && it.userId == userId }

    private fun isModerator(userId: String) = users.findById(userId)?.isModerator == true

    private fun isMostRelevantOfItsMonth(event: CommunityEvent): Boolean {
        val month = YearMonth.from(event.startsAt)
        val top = _publicEvents.value.filter { YearMonth.from(it.startsAt) == month }.maxByOrNull { it.interestCount }
        return top?.id == event.id
    }

    private fun replace(updated: CommunityEvent) = setEvents(_events.value.map { if (it.id == updated.id) updated else it })

    private fun setEvents(events: List<CommunityEvent>) {
        _events.value = events
        _publicEvents.value = events.filter(::isPublic)
    }

    private fun isPublic(event: CommunityEvent) = event.status == EventStatus.VERIFIED

    companion object {
        /** Instancia compartida por las pantallas mientras la app esté abierta (aún no hay inyección de dependencias). */
        val shared: EventRepository by lazy {
            InMemoryEventRepository(notifications = InMemoryNotificationRepository.shared, users = InMemoryUserRepository.shared)
        }
    }
}
