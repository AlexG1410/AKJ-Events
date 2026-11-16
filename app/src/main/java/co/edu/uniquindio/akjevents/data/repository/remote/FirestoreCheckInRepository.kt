package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.remote.event.AttendanceDto
import co.edu.uniquindio.akjevents.data.remote.event.participationId
import co.edu.uniquindio.akjevents.data.remote.user.PointsEntryDto
import co.edu.uniquindio.akjevents.data.remote.user.UserDto
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import co.edu.uniquindio.akjevents.data.remote.event.EventDto
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

/** Sin claves de servidor. La transacción protege reintentos legítimos entre dispositivos. */
class FirestoreCheckInRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CheckInRepository {
    override suspend fun complete(eventId: String, userId: String, reward: Int): CheckInCommit {
        require(reward == ReputationPoints.CHECK_IN)
        check(auth.currentUser?.uid == userId) { "CHECK_IN_SESSION" }
        val eventRef = firestore.collection("events").document(eventId)
        val attendanceRef = firestore.collection("attendances").document(participationId(eventId, userId))
        val userRef = firestore.collection("users").document(userId)
        // ID permanente por cuenta/evento; también actúa como marcador de premio ya concedido.
        val historyRef = firestore.collection("pointsHistory").document("checkin_${participationId(eventId, userId)}")
        val createdAt = LocalDateTime.now()
        return try { firestore.runTransaction { tx ->
            val event = tx.get(eventRef).toObject(EventDto::class.java)?.toDomain(eventId)
                ?.takeIf { it.status == EventStatus.VERIFIED } ?: return@runTransaction CheckInCommit(CheckInRegistration.NOT_FOUND)
            if (event.organizerId == userId) return@runTransaction CheckInCommit(CheckInRegistration.OWN_EVENT, event)
            val attendance = tx.get(attendanceRef).toObject(AttendanceDto::class.java)
            val user = tx.get(userRef).toObject(UserDto::class.java)?.toDomain()
                ?: return@runTransaction CheckInCommit(CheckInRegistration.NOT_FOUND)
            // Los check-ins históricos sin marcador no se vuelven a premiar ni se migran automáticamente.
            if (attendance?.checkedIn == true) return@runTransaction CheckInCommit(CheckInRegistration.ALREADY_CHECKED_IN, event)
            if (attendance == null && event.capacity != null && event.attendanceCount >= event.capacity) return@runTransaction CheckInCommit(CheckInRegistration.FULL, event)
            val earned = if (user.isModerator) 0 else reward
            val updated = user.copy(points = user.points + earned)
            if (attendance == null) {
                tx.update(eventRef, "attendanceCount", event.attendanceCount + 1)
                tx.set(attendanceRef, AttendanceDto(eventId, userId, checkedIn = true))
            } else tx.update(attendanceRef, "checkedIn", true)
            if (earned > 0) {
                tx.update(userRef, "points", updated.points)
                // Las reglas existentes solo permiten create, no update de historial: un ID previo
                // provoca rechazo y rollback de toda la transacción, nunca un segundo premio.
                tx.set(historyRef, PointsEntryDto.fromDomain(PointsEntry(userId, earned, PointsReason.CHECK_IN, createdAt, event.title)).copy(eventId = eventId))
            }
            CheckInCommit(CheckInRegistration.REGISTERED, event, earned, user, updated)
        }.await() } catch (error: FirebaseFirestoreException) {
            // Algunas carreras se resuelven como PERMISSION_DENIED al intentar reescribir el
            // historial inmutable, antes de que el SDK reintente. Confirmar SOLO desde servidor.
            if (error.code in setOf(FirebaseFirestoreException.Code.PERMISSION_DENIED, FirebaseFirestoreException.Code.ABORTED, FirebaseFirestoreException.Code.UNAVAILABLE)) {
                try {
                    val checked = attendanceRef.get(Source.SERVER).await().toObject(AttendanceDto::class.java)?.checkedIn == true
                    val currentEvent = eventRef.get(Source.SERVER).await().toObject(EventDto::class.java)?.toDomain(eventId)
                    if (checked && auth.currentUser?.uid == userId && currentEvent != null && currentEvent.organizerId != userId) {
                        return CheckInCommit(CheckInRegistration.ALREADY_CHECKED_IN, currentEvent)
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled } catch (_: Exception) { /* Conservar error original; no inventar éxito offline. */ }
            }
            throw error
        }
    }
}
