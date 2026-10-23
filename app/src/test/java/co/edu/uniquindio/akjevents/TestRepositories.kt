package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryNotificationRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository

/**
 * Repositorios en memoria nuevos y conectados entre sí, para que cada prueba empiece con los datos de muestra.
 * Las notificaciones arrancan vacías y la sesión queda abierta con [sessionUserId] (el usuario de demostración).
 */
class TestRepositories(sessionUserId: String? = SampleUsers.demoUser.id) {
    val notifications = InMemoryNotificationRepository(emptyList())
    val users = InMemoryUserRepository(notifications = notifications, sessionUserId = sessionUserId)
    val events = InMemoryEventRepository(notifications = notifications, users = users)

    fun notificationsOf(userId: String) = notifications.notifications.value.filter { it.userId == userId }

    fun pointsOf(userId: String) = users.findById(userId)!!.points

    companion object {
        val DEMO_USER = SampleUsers.demoUser.id
        val MODERATOR = SampleUsers.moderators.first().id
    }
}
