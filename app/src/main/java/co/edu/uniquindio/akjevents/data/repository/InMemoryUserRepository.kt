package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.repository.PointsChange
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cuentas y sesión en memoria. Las contraseñas se guardan en claro solo porque son de demostración:
 * con el backend de la siguiente fase la autenticación la hará el servicio externo.
 * Hilt crea una sola instancia con los datos de muestra; las pruebas usan el constructor principal.
 */
@Singleton
class InMemoryUserRepository(
    initialUsers: List<User>,
    initialPasswords: Map<String, String> = SampleUsers.passwords,
    initialPointsHistory: List<PointsEntry> = SampleUsers.pointsHistory(),
    sessionUserId: String? = null,
    private val now: () -> LocalDateTime = LocalDateTime::now
) : UserRepository {

    @Inject
    constructor() : this(SampleUsers.all)

    private val lock = Any()
    // Por correo en minúsculas
    private val passwords = initialPasswords.mapKeys { it.key.lowercase() }.toMutableMap()

    private val _users = MutableStateFlow(initialUsers)
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _currentUser = MutableStateFlow(initialUsers.find { it.id == sessionUserId })
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _pointsHistory = MutableStateFlow(initialPointsHistory)
    override val pointsHistory: StateFlow<List<PointsEntry>> = _pointsHistory.asStateFlow()

    override suspend fun findById(id: String): User? = userById(id)

    override suspend fun login(email: String, password: String): User? = synchronized(lock) {
        val normalized = email.trim().lowercase()
        val user = _users.value.find { it.email.lowercase() == normalized }
        if (user == null || passwords[normalized] != password) return null
        _currentUser.value = user
        user
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun register(
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

    override suspend fun updateProfile(updated: User): ProfileUpdateResult = synchronized(lock) {
        val existing = userById(updated.id) ?: return ProfileUpdateResult.NOT_FOUND
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

    override suspend fun deleteAccount(userId: String): Boolean = synchronized(lock) {
        val user = userById(userId) ?: return false
        _users.update { list -> list.filterNot { it.id == userId } }
        passwords.remove(user.email.lowercase())
        _pointsHistory.update { list -> list.filterNot { it.userId == userId } }
        if (_currentUser.value?.id == userId) _currentUser.value = null
        true
    }

    override suspend fun addPoints(userId: String, points: Int, reason: String): PointsChange? = synchronized(lock) {
        val user = userById(userId) ?: return null
        val updated = user.copy(points = (user.points + points).coerceAtLeast(0))
        replace(updated)
        _pointsHistory.update { listOf(PointsEntry(userId, points, reason, now())) + it }
        PointsChange(before = user, after = updated)
    }

    override suspend fun addBadge(userId: String, badge: Badge): Boolean = synchronized(lock) {
        val user = userById(userId) ?: return false
        if (badge in user.badges) return false
        replace(user.copy(badges = user.badges + badge))
        true
    }

    // Lectura sin suspensión, para usarla dentro de las secciones sincronizadas
    private fun userById(id: String): User? = _users.value.find { it.id == id }

    private fun replace(user: User) {
        _users.update { list -> list.map { if (it.id == user.id) user else it } }
        if (_currentUser.value?.id == user.id) _currentUser.value = user
    }
}
