package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import kotlinx.coroutines.flow.StateFlow

sealed interface RegisterResult {
    data class Success(val user: User) : RegisterResult
    data object EmailInUse : RegisterResult
}

enum class ProfileUpdateResult { UPDATED, EMAIL_IN_USE, NOT_FOUND }

/** Cuentas de usuario, sesión actual y reputación (puntos, niveles e insignias). */
interface UserRepository {
    val users: StateFlow<List<User>>

    /** Usuario con la sesión abierta; null si nadie ha iniciado sesión. */
    val currentUser: StateFlow<User?>

    val pointsHistory: StateFlow<List<PointsEntry>>

    fun findById(id: String): User?

    /** Abre la sesión si el correo y la contraseña coinciden. */
    fun login(email: String, password: String): User?

    fun logout()

    /** Crea la cuenta como Usuario (nivel Espectador) y abre su sesión. */
    fun register(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult

    /** Cambia los datos personales; el rol, los puntos y las insignias no se modifican aquí. */
    fun updateProfile(updated: User): ProfileUpdateResult

    /** Elimina la cuenta y cierra la sesión si era la del usuario actual. */
    fun deleteAccount(userId: String): Boolean

    /** Suma (o resta) puntos y avisa si el usuario sube de nivel. */
    fun addPoints(userId: String, points: Int, reason: String)

    /** Otorga una insignia una sola vez y la notifica. */
    fun awardBadge(userId: String, badge: Badge)
}
