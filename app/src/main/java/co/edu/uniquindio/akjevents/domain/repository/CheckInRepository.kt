package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User

data class CheckInCommit(
    val registration: CheckInRegistration,
    val event: CommunityEvent? = null,
    val pointsEarned: Int = 0,
    val before: User? = null,
    val after: User? = null
)

/** Asistencia, saldo e historial se confirman juntos. Un reintento no vuelve a premiar. */
fun interface CheckInRepository {
    suspend fun complete(eventId: String, userId: String, reward: Int): CheckInCommit
}
