package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda los eventos en memoria: los cambios duran mientras la app esté abierta. */
class InMemoryEventRepository(initialEvents: List<CommunityEvent> = SampleEvents.all) : EventRepository {

    // Todos los eventos, incluidos los que no son públicos (pendientes, rechazados...)
    private var events: List<CommunityEvent> = initialEvents
    private val lock = Any()

    private val _publicEvents = MutableStateFlow(events.filter(::isPublic))
    override val publicEvents: StateFlow<List<CommunityEvent>> = _publicEvents.asStateFlow()

    private val _attendingEventIds = MutableStateFlow<Set<String>>(emptySet())
    override val attendingEventIds: StateFlow<Set<String>> = _attendingEventIds.asStateFlow()

    private val _interestedEventIds = MutableStateFlow<Set<String>>(emptySet())
    override val interestedEventIds: StateFlow<Set<String>> = _interestedEventIds.asStateFlow()

    override fun findPublicEvent(id: String): CommunityEvent? = _publicEvents.value.find { it.id == id }

    override fun findEventsByOrganizer(organizerId: String): List<CommunityEvent> =
        events.filter { it.organizerId == organizerId }

    override fun createEvent(event: CommunityEvent) = synchronized(lock) {
        require(events.none { it.id == event.id }) { "Ya existe un evento con el id ${event.id}" }
        events = events + event
        _publicEvents.value = events.filter(::isPublic)
    }

    override fun confirmAttendance(eventId: String): AttendanceResult = synchronized(lock) {
        val event = findPublicEvent(eventId) ?: return AttendanceResult.NOT_FOUND
        when {
            eventId in _attendingEventIds.value -> AttendanceResult.ALREADY_CONFIRMED
            event.capacity != null && event.attendanceCount >= event.capacity -> AttendanceResult.FULL
            else -> {
                replace(event.copy(attendanceCount = event.attendanceCount + 1))
                _attendingEventIds.value += eventId
                AttendanceResult.CONFIRMED
            }
        }
    }

    override fun cancelAttendance(eventId: String): Boolean = synchronized(lock) {
        val event = findPublicEvent(eventId)
        if (event == null || eventId !in _attendingEventIds.value) return false
        replace(event.copy(attendanceCount = (event.attendanceCount - 1).coerceAtLeast(0)))
        _attendingEventIds.value -= eventId
        true
    }

    override fun toggleInterest(eventId: String): Boolean? = synchronized(lock) {
        val event = findPublicEvent(eventId) ?: return null
        val interested = eventId !in _interestedEventIds.value
        replace(event.copy(interestCount = (event.interestCount + if (interested) 1 else -1).coerceAtLeast(0)))
        _interestedEventIds.value = if (interested) _interestedEventIds.value + eventId else _interestedEventIds.value - eventId
        interested
    }

    private fun replace(updated: CommunityEvent) {
        events = events.map { if (it.id == updated.id) updated else it }
        _publicEvents.value = events.filter(::isPublic)
    }

    private fun isPublic(event: CommunityEvent) = event.status == EventStatus.VERIFIED

    companion object {
        /** Instancia compartida por las pantallas mientras la app esté abierta (aún no hay inyección de dependencias). */
        val shared: EventRepository by lazy { InMemoryEventRepository() }
    }
}
