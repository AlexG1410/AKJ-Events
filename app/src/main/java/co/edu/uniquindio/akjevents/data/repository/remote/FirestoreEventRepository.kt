package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.remote.event.AttendanceDto
import co.edu.uniquindio.akjevents.data.remote.event.CommentDto
import co.edu.uniquindio.akjevents.data.remote.event.EventDto
import co.edu.uniquindio.akjevents.data.remote.event.InterestDto
import co.edu.uniquindio.akjevents.data.remote.event.participationId
import co.edu.uniquindio.akjevents.data.remote.listen
import co.edu.uniquindio.akjevents.di.ApplicationScope
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.CheckInRegistration
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Eventos y participación en Cloud Firestore (guía 19), en las colecciones `events`, `comments`,
 * `attendances` e `interests`. Las listas se escuchan en tiempo real mientras haya una sesión abierta.
 *
 * Las reglas de seguridad (`firestore.rules`) solo dejan leer los eventos verificados o finalizados, los
 * propios y, para los moderadores, todos; por eso la consulta de eventos depende del rol de la sesión.
 * Los cambios de cupo y de conteos se hacen en transacciones, para que dos personas no tomen el último cupo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirestoreEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    userRepository: UserRepository,
    @ApplicationScope private val scope: CoroutineScope
) : EventRepository {

    private val eventsCollection = firestore.collection(EVENTS)
    private val commentsCollection = firestore.collection(COMMENTS)
    private val attendancesCollection = firestore.collection(ATTENDANCES)
    private val interestsCollection = firestore.collection(INTERESTS)

    /** Quién tiene la sesión abierta y si es moderador; las consultas cambian con él. */
    private val session: Flow<Session?> = userRepository.currentUser
        .map { user -> user?.let { Session(it.id, it.role == UserRole.MODERATOR) } }
        .distinctUntilChanged()

    override val events: StateFlow<List<CommunityEvent>> = session
        .flatMapLatest { session ->
            when {
                session == null -> flowOf(emptyList())
                session.isModerator -> eventsCollection.listen(::toEvent)
                // Los públicos (verificados y finalizados) más los propios en cualquier estado
                else -> combine(
                    eventsCollection.whereIn(FIELD_STATUS, VISIBLE_STATUSES).listen(::toEvent),
                    eventsCollection.whereEqualTo(FIELD_ORGANIZER_ID, session.userId).listen(::toEvent)
                ) { visible, own -> (visible + own).distinctBy { it.id } }
            }
        }
        .map { list -> list.sortedBy { it.startsAt } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val publicEvents: StateFlow<List<CommunityEvent>> = events
        .map { list -> list.filter { it.status == EventStatus.VERIFIED } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val comments: StateFlow<List<Comment>> = whileSignedIn {
        commentsCollection.listen { doc -> doc.toObject(CommentDto::class.java)?.toDomain(doc.id) }
            .map { list -> list.sortedBy { it.createdAt } }
    }

    override val attendances: StateFlow<List<Attendance>> = whileSignedIn {
        attendancesCollection.listen { it.toObject(AttendanceDto::class.java)?.toDomain() }
    }

    override val interests: StateFlow<List<Interest>> = whileSignedIn {
        interestsCollection.listen { it.toObject(InterestDto::class.java)?.toDomain() }
    }

    override suspend fun findEvent(id: String): CommunityEvent? = try {
        toEvent(eventsCollection.document(id).get().await())
    } catch (e: FirebaseFirestoreException) {
        // Las reglas no dejan leer los eventos pendientes o rechazados de otras personas
        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) null else throw e
    }

    override suspend fun findPublicEvent(id: String): CommunityEvent? =
        findEvent(id)?.takeIf { it.status == EventStatus.VERIFIED }

    override suspend fun findPublicEventByCheckInCode(code: String): CommunityEvent? =
        eventsCollection
            .whereEqualTo(FIELD_CHECK_IN_CODE, code)
            .whereEqualTo(FIELD_STATUS, EventStatus.VERIFIED.name)
            .get().await()
            .documents.firstNotNullOfOrNull(::toEvent)

    override suspend fun findEventsByOrganizer(organizerId: String): List<CommunityEvent> =
        eventsCollection.whereEqualTo(FIELD_ORGANIZER_ID, organizerId).get().await().documents.mapNotNull(::toEvent)

    override suspend fun createEvent(event: CommunityEvent) {
        val reference = eventsCollection.document(event.id)
        firestore.runTransaction { transaction ->
            require(!transaction.get(reference).exists()) { "Ya existe un evento con el id ${event.id}" }
            transaction.set(reference, EventDto.fromDomain(event))
        }.await()
    }

    override suspend fun updateEvent(event: CommunityEvent): CommunityEvent? {
        val reference = eventsCollection.document(event.id)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)
            val existing = toEvent(snapshot) ?: return@runTransaction null
            // Los conteos y el organizador no se pueden alterar desde el formulario
            val updated = event.copy(
                organizerId = existing.organizerId,
                attendanceCount = existing.attendanceCount,
                interestCount = existing.interestCount,
                createdAt = existing.createdAt
            )
            transaction.set(reference, EventDto.fromDomain(updated, snapshot.verificationRewarded()))
            updated
        }.await()
    }

    override suspend fun updateStatus(eventId: String, status: EventStatus, rejectionReason: String?): CommunityEvent? {
        val reference = eventsCollection.document(eventId)
        return firestore.runTransaction { transaction ->
            val event = toEvent(transaction.get(reference)) ?: return@runTransaction null
            val reason = if (status == EventStatus.REJECTED) rejectionReason else null
            transaction.update(reference, FIELD_STATUS, status.name, FIELD_REJECTION_REASON, reason)
            event.copy(status = status, rejectionReason = reason)
        }.await()
    }

    override suspend fun registerVerificationReward(eventId: String): Boolean {
        val reference = eventsCollection.document(eventId)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(reference)
            if (!snapshot.exists() || snapshot.verificationRewarded()) return@runTransaction false
            transaction.update(reference, FIELD_VERIFICATION_REWARDED, true)
            true
        }.await()
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        val reference = eventsCollection.document(eventId)
        if (!reference.get().await().exists()) return false
        val related = relatedDocuments(eventId)
        firestore.runBatch { batch ->
            related.forEach(batch::delete)
            batch.delete(reference)
        }.await()
        return true
    }

    override suspend fun confirmAttendance(eventId: String, userId: String): AttendanceResult {
        val eventReference = eventsCollection.document(eventId)
        val attendanceReference = attendancesCollection.document(participationId(eventId, userId))
        return firestore.runTransaction { transaction ->
            val event = toEvent(transaction.get(eventReference)) ?: return@runTransaction AttendanceResult.NOT_FOUND
            val alreadyAttending = transaction.get(attendanceReference).exists()
            when {
                event.status == EventStatus.FINISHED -> AttendanceResult.CLOSED
                event.status != EventStatus.VERIFIED -> AttendanceResult.NOT_FOUND
                alreadyAttending -> AttendanceResult.ALREADY_CONFIRMED
                event.isFull -> AttendanceResult.FULL
                else -> {
                    transaction.update(eventReference, FIELD_ATTENDANCE_COUNT, event.attendanceCount + 1)
                    transaction.set(attendanceReference, AttendanceDto(eventId, userId))
                    AttendanceResult.CONFIRMED
                }
            }
        }.await()
    }

    override suspend fun cancelAttendance(eventId: String, userId: String): Boolean {
        val eventReference = eventsCollection.document(eventId)
        val attendanceReference = attendancesCollection.document(participationId(eventId, userId))
        return firestore.runTransaction { transaction ->
            val event = toEvent(transaction.get(eventReference))?.takeIf { it.status == EventStatus.VERIFIED }
            val attendance = transaction.get(attendanceReference).toObject(AttendanceDto::class.java)
            if (event == null || attendance == null || attendance.checkedIn) return@runTransaction false
            transaction.update(eventReference, FIELD_ATTENDANCE_COUNT, (event.attendanceCount - 1).coerceAtLeast(0))
            transaction.delete(attendanceReference)
            true
        }.await()
    }

    override suspend fun registerCheckIn(eventId: String, userId: String): CheckInRegistration {
        val eventReference = eventsCollection.document(eventId)
        val attendanceReference = attendancesCollection.document(participationId(eventId, userId))
        return firestore.runTransaction { transaction ->
            val event = toEvent(transaction.get(eventReference))?.takeIf { it.status == EventStatus.VERIFIED }
                ?: return@runTransaction CheckInRegistration.NOT_FOUND
            val attendance = transaction.get(attendanceReference).toObject(AttendanceDto::class.java)
            when {
                attendance?.checkedIn == true -> return@runTransaction CheckInRegistration.ALREADY_CHECKED_IN
                attendance != null -> transaction.update(attendanceReference, FIELD_CHECKED_IN, true)
                event.isFull -> return@runTransaction CheckInRegistration.FULL
                else -> {
                    // Quien llega sin haber confirmado ocupa un cupo
                    transaction.update(eventReference, FIELD_ATTENDANCE_COUNT, event.attendanceCount + 1)
                    transaction.set(attendanceReference, AttendanceDto(eventId, userId, checkedIn = true))
                }
            }
            CheckInRegistration.REGISTERED
        }.await()
    }

    override suspend fun toggleInterest(eventId: String, userId: String): Boolean? {
        val eventReference = eventsCollection.document(eventId)
        val interestReference = interestsCollection.document(participationId(eventId, userId))
        return firestore.runTransaction { transaction ->
            val event = toEvent(transaction.get(eventReference))?.takeIf { it.status == EventStatus.VERIFIED }
                ?: return@runTransaction null
            val interested = !transaction.get(interestReference).exists()
            val count = (event.interestCount + if (interested) 1 else -1).coerceAtLeast(0)
            transaction.update(eventReference, FIELD_INTEREST_COUNT, count)
            if (interested) transaction.set(interestReference, InterestDto(eventId, userId)) else transaction.delete(interestReference)
            interested
        }.await()
    }

    override suspend fun addComment(eventId: String, authorId: String, text: String): Comment {
        val reference = commentsCollection.document()
        val comment = Comment(reference.id, eventId, authorId, text, LocalDateTime.now())
        reference.set(CommentDto.fromDomain(comment)).await()
        return comment
    }

    override suspend fun removeUserData(userId: String) {
        val ownEvents = eventsCollection.whereEqualTo(FIELD_ORGANIZER_ID, userId).get().await().documents
        val ownEventIds = ownEvents.map { it.id }.toSet()
        val attendances = attendancesCollection.whereEqualTo(FIELD_USER_ID, userId).get().await().documents
        val interests = interestsCollection.whereEqualTo(FIELD_USER_ID, userId).get().await().documents
        val comments = commentsCollection.whereEqualTo(FIELD_AUTHOR_ID, userId).get().await().documents
        val related = ownEventIds.flatMap { relatedDocuments(it) }
        // Sus asistencias e intereses dejan de contar en los eventos de los demás que siguen existiendo
        val othersEvents = (attendances + interests)
            .mapNotNull { it.getString(FIELD_EVENT_ID) }
            .filterNot { it in ownEventIds }
            .distinct()
            .filter { eventsCollection.document(it).get().await().exists() }
            .toSet()

        firestore.runBatch { batch ->
            attendances.forEach { batch.discount(it, othersEvents, FIELD_ATTENDANCE_COUNT) }
            interests.forEach { batch.discount(it, othersEvents, FIELD_INTEREST_COUNT) }
            (comments.map { it.reference } + related).distinct().forEach(batch::delete)
            ownEvents.forEach { batch.delete(it.reference) }
        }.await()
    }

    /** Comentarios, asistencias e intereses de un evento. */
    private suspend fun relatedDocuments(eventId: String): List<DocumentReference> =
        listOf(commentsCollection, attendancesCollection, interestsCollection).flatMap { collection ->
            collection.whereEqualTo(FIELD_EVENT_ID, eventId).get().await().documents.map { it.reference }
        }

    /** Borra una asistencia o un interés y lo descuenta del evento, si este sigue existiendo. */
    private fun WriteBatch.discount(participation: DocumentSnapshot, existingEvents: Set<String>, countField: String) {
        val eventId = participation.getString(FIELD_EVENT_ID)
        if (eventId != null && eventId in existingEvents) {
            update(eventsCollection.document(eventId), countField, FieldValue.increment(-1))
        }
        delete(participation.reference)
    }

    /** Lista que se escucha solo mientras hay una sesión (las reglas exigen haber iniciado sesión). */
    private fun <T> whileSignedIn(listen: () -> Flow<List<T>>): StateFlow<List<T>> = session
        .map { it?.userId }
        .distinctUntilChanged()
        .flatMapLatest { userId -> if (userId == null) flowOf(emptyList()) else listen() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private fun toEvent(snapshot: DocumentSnapshot): CommunityEvent? =
        if (snapshot.exists()) snapshot.toObject(EventDto::class.java)?.toDomain(snapshot.id) else null

    private fun DocumentSnapshot.verificationRewarded(): Boolean = getBoolean(FIELD_VERIFICATION_REWARDED) == true

    private val CommunityEvent.isFull: Boolean get() = capacity != null && attendanceCount >= capacity

    private data class Session(val userId: String, val isModerator: Boolean)

    private companion object {
        const val EVENTS = "events"
        const val COMMENTS = "comments"
        const val ATTENDANCES = "attendances"
        const val INTERESTS = "interests"
        const val FIELD_STATUS = "status"
        const val FIELD_ORGANIZER_ID = "organizerId"
        const val FIELD_EVENT_ID = "eventId"
        const val FIELD_USER_ID = "userId"
        const val FIELD_AUTHOR_ID = "authorId"
        const val FIELD_CHECK_IN_CODE = "checkInCode"
        const val FIELD_REJECTION_REASON = "rejectionReason"
        const val FIELD_ATTENDANCE_COUNT = "attendanceCount"
        const val FIELD_INTEREST_COUNT = "interestCount"
        const val FIELD_CHECKED_IN = "checkedIn"
        const val FIELD_VERIFICATION_REWARDED = "verificationRewarded"
        val VISIBLE_STATUSES = listOf(EventStatus.VERIFIED.name, EventStatus.FINISHED.name)
    }
}
