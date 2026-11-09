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

/** El usuario antes y después de un cambio de puntos, para saber si subió de nivel. */
data class PointsChange(val before: User, val after: User)

/**
 * Cuentas de usuario, sesión actual y datos de reputación (puntos e insignias).
 * Las reglas de la reputación (quién suma puntos, qué se notifica) están en los casos de uso.
 */
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

    /** Elimina la cuenta con su historial de puntos y cierra la sesión si era la del usuario actual. */
    fun deleteAccount(userId: String): Boolean

    /** Suma (o resta) puntos, sin bajar de 0, y guarda el movimiento. Devuelve null si el usuario no existe. */
    fun addPoints(userId: String, points: Int, reason: String): PointsChange?

    /** Agrega la insignia. Devuelve false si el usuario ya la tenía o no existe. */
    fun addBadge(userId: String, badge: Badge): Boolean
}
