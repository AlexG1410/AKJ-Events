package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.remote.listen
import co.edu.uniquindio.akjevents.data.remote.notification.NotificationDto
import co.edu.uniquindio.akjevents.di.ApplicationScope
import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
 * Notificaciones en la colección `notifications` de Firestore. Cada persona solo escucha las suyas
 * (las reglas de seguridad no dejan leer las de otros); cualquiera con sesión puede crearle una a otra
 * persona, por ejemplo al comentar en su evento.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirestoreNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    userRepository: UserRepository,
    @ApplicationScope scope: CoroutineScope
) : NotificationRepository {

    private val notificationsCollection = firestore.collection(NOTIFICATIONS)

    override val notifications: StateFlow<List<AppNotification>> = userRepository.currentUser
        .map { it?.id }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList())
            } else {
                notificationsCollection.whereEqualTo(FIELD_USER_ID, userId)
                    .listen { doc -> doc.toObject(NotificationDto::class.java)?.toDomain(doc.id) }
                    // La más reciente va primero
                    .map { list -> list.sortedByDescending { it.createdAt } }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun notify(
        userId: String,
        type: NotificationType,
        eventId: String?,
        eventTitle: String?,
        actorName: String?,
        detail: String?,
        badge: Badge?,
        level: UserLevel?
    ) {
        val reference = notificationsCollection.document()
        val notification = AppNotification(
            id = reference.id,
            userId = userId,
            type = type,
            createdAt = LocalDateTime.now(),
            eventId = eventId,
            eventTitle = eventTitle,
            actorName = actorName,
            detail = detail,
            badge = badge,
            level = level
        )
        reference.set(NotificationDto.fromDomain(notification)).await()
    }

    override suspend fun setRead(notificationId: String, isRead: Boolean) {
        notificationsCollection.document(notificationId).update(FIELD_READ, isRead).await()
    }

    override suspend fun markAllAsRead(userId: String) {
        val unread = notificationsCollection.whereEqualTo(FIELD_USER_ID, userId).whereEqualTo(FIELD_READ, false).get().await()
        if (unread.isEmpty) return
        firestore.runBatch { batch -> unread.documents.forEach { batch.update(it.reference, FIELD_READ, true) } }.await()
    }

    override suspend fun deleteForUser(userId: String) {
        val own = notificationsCollection.whereEqualTo(FIELD_USER_ID, userId).get().await()
        if (own.isEmpty) return
        firestore.runBatch { batch -> own.documents.forEach { batch.delete(it.reference) } }.await()
    }

    private companion object {
        const val NOTIFICATIONS = "notifications"
        const val FIELD_USER_ID = "userId"
        const val FIELD_READ = "read"
    }
}
