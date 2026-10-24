package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID

/**
 * Cuentas y sesión en memoria. Las contraseñas se guardan en claro solo porque son de demostración:
 * con el backend de la siguiente fase la autenticación la hará el servicio externo.
 */
class InMemoryUserRepository(
    initialUsers: List<User> = SampleUsers.all,
    initialPasswords: Map<String, String> = SampleUsers.passwords,
    initialPointsHistory: List<PointsEntry> = SampleUsers.pointsHistory(),
    private val notifications: NotificationRepository = InMemoryNotificationRepository(emptyList()),
    sessionUserId: String? = null,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : UserRepository {

    private val lock = Any()
    // Por correo en minúsculas
    private val passwords = initialPasswords.mapKeys { it.key.lowercase() }.toMutableMap()

    private val _users = MutableStateFlow(initialUsers)
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow(initialUsers.find { it.id == sessionUserId })
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _pointsHistory = MutableStateFlow(initialPointsHistory)
    override val pointsHistory: StateFlow<List<PointsEntry>> = _pointsHistory.asStateFlow()

    override fun findById(id: String): User? = _users.value.find { it.id == id }

    override fun login(email: String, password: String): User? = synchronized(lock) {
        val normalized = email.trim().lowercase()
        val user = _users.value.find { it.email.lowercase() == normalized }
        if (user == null || passwords[normalized] != password) return null
        _currentUser.value = user
        user
    }

    override fun logout() {
        _currentUser.value = null
    }

    override fun register(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult = synchronized(lock) {
        val normalized = email.trim().lowercase()
        if (_users.value.any { it.email.lowercase() == normalized }) return RegisterResult.EmailInUse

        val user = User(
            id = "user-${UUID.randomUUID()}",
            name = name.trim(),
            email = email.trim(),
            city = city,
            address = address.trim(),
            location = location,
            role = UserRole.USER,
            interests = interests
        )
        _users.update { it + user }
        passwords[normalized] = password
        _currentUser.value = user
        RegisterResult.Success(user)
    }

    override fun updateProfile(updated: User): ProfileUpdateResult = synchronized(lock) {
        val existing = findById(updated.id) ?: return ProfileUpdateResult.NOT_FOUND
        val newEmail = updated.email.trim()
        val emailTaken = _users.value.any { it.id != updated.id && it.email.equals(newEmail, ignoreCase = true) }
        if (emailTaken) return ProfileUpdateResult.EMAIL_IN_USE

        // Solo los datos personales: rol, puntos e insignias los maneja la app
        val saved = existing.copy(
            name = updated.name.trim(),
            email = newEmail,
            city = updated.city,
            address = updated.address,
            location = updated.location,
            interests = updated.interests,
            profilePictureUrl = updated.profilePictureUrl
        )
        if (!existing.email.equals(newEmail, ignoreCase = true)) {
            passwords.remove(existing.email.lowercase())?.let { passwords[newEmail.lowercase()] = it }
        }
        replace(saved)
        ProfileUpdateResult.UPDATED
    }

    override fun deleteAccount(userId: String): Boolean = synchronized(lock) {
        val user = findById(userId) ?: return false
        _users.update { list -> list.filterNot { it.id == userId } }
        passwords.remove(user.email.lowercase())
        _pointsHistory.update { list -> list.filterNot { it.userId == userId } }
        notifications.deleteForUser(userId)
        if (_currentUser.value?.id == userId) _currentUser.value = null
        true
    }

    override fun addPoints(userId: String, points: Int, reason: String) {
        if (points == 0) return
        val levelUp = synchronized(lock) {
            val user = findById(userId) ?: return
            // Los moderadores no participan del sistema de reputación
            if (user.isModerator) return
            val updated = user.copy(points = (user.points + points).coerceAtLeast(0))
            replace(updated)
            _pointsHistory.update { listOf(PointsEntry(userId, points, reason, now())) + it }
            updated.level.takeIf { it.ordinal > user.level.ordinal }
        }
        if (levelUp != null) {
            notifications.notify(
                userId = userId,
                type = NotificationType.LEVEL_UP,
                title = "¡Ascendiste a ${levelUp.label}!",
                message = "Alcanzaste ${levelUp.minPoints} puntos cívicos y ahora eres nivel ${levelUp.number}: ${levelUp.label}."
            )
        }
    }

    override fun awardBadge(userId: String, badge: Badge) {
        synchronized(lock) {
            val user = findById(userId) ?: return
            if (user.isModerator || badge in user.badges) return
            replace(user.copy(badges = user.badges + badge))
        }
        notifications.notify(
            userId = userId,
            type = NotificationType.BADGE_UNLOCKED,
            title = "Nueva insignia: ${badge.label}",
            message = "${badge.description}. ¡Sigue participando!"
        )
    }

    private fun replace(user: User) {
        _users.update { list -> list.map { if (it.id == user.id) user else it } }
        if (_currentUser.value?.id == user.id) _currentUser.value = user
    }

    companion object {
        /** Instancia compartida por las pantallas mientras la app esté abierta (aún no hay inyección de dependencias). */
        val shared: UserRepository by lazy { InMemoryUserRepository(notifications = InMemoryNotificationRepository.shared) }
    }
}
