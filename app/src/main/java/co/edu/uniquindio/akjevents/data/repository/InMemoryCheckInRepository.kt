package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.repository.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Solo fixtures locales: el backend activo es FirestoreCheckInRepository. */
class InMemoryCheckInRepository(private val events: EventRepository, private val users: UserRepository) : CheckInRepository {
    private val mutex = Mutex()
    override suspend fun complete(eventId: String, userId: String, reward: Int): CheckInCommit = mutex.withLock {
        val event = events.findPublicEvent(eventId) ?: return@withLock CheckInCommit(CheckInRegistration.NOT_FOUND)
        val user = users.findById(userId) ?: return@withLock CheckInCommit(CheckInRegistration.NOT_FOUND)
        val registration = events.registerCheckIn(eventId, userId)
        if (registration != CheckInRegistration.REGISTERED) return@withLock CheckInCommit(registration, event)
        val earned = if (user.isModerator) 0 else reward
        val change = if (earned > 0) users.addPoints(userId, earned, PointsReason.CHECK_IN, event.title) else null
        CheckInCommit(registration, event, earned, user, change?.after ?: user)
    }
}
