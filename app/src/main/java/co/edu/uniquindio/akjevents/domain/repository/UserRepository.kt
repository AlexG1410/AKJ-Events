package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.User
import kotlinx.coroutines.flow.StateFlow

sealed interface RegisterResult {
    data class Success(val user: User) : RegisterResult
    data object EmailInUse : RegisterResult
}

/** [EMAIL_VERIFICATION_SENT]: los demás datos se guardaron y el nuevo correo espera la confirmación del enlace enviado. */
enum class ProfileUpdateResult { UPDATED, EMAIL_VERIFICATION_SENT, EMAIL_IN_USE, NOT_FOUND }

/** El usuario antes y después de un cambio de puntos, para saber si subió de nivel. */
data class PointsChange(val before: User, val after: User)

/**
 * Cuentas de usuario, sesión actual y datos de reputación (puntos e insignias).
 * Las reglas de la reputación (quién suma puntos, qué se notifica) están en los casos de uso.
 */
interface UserRepository {
    /** Usuario con la sesión abierta; null si nadie ha iniciado sesión. */
    val currentUser: StateFlow<User?>

    /**
     * False mientras se averigua si quedó una sesión abierta (por ejemplo, al abrir la app con Firebase);
     * así la navegación no muestra el login por un instante antes de entrar.
     */
    val isSessionReady: StateFlow<Boolean>

    val pointsHistory: StateFlow<List<PointsEntry>>

    suspend fun findById(id: String): User?

    /** Abre la sesión si el correo y la contraseña coinciden. */
    suspend fun login(email: String, password: String): User?

    suspend fun logout()

    /** Envía al correo un enlace para restablecer la contraseña. No revela si el correo tiene cuenta. */
    suspend fun sendPasswordReset(email: String)

    /** Crea la cuenta como Usuario (nivel Espectador) y abre su sesión. */
    suspend fun register(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult

    /** Cambia los datos personales; el rol, los puntos y las insignias no se modifican aquí. */
    suspend fun updateProfile(updated: User): ProfileUpdateResult

    /** Elimina la cuenta con su historial de puntos y cierra la sesión si era la del usuario actual. */
    suspend fun deleteAccount(userId: String): Boolean

    /** Suma (o resta) puntos, sin bajar de 0, y guarda el movimiento. Devuelve null si el usuario no existe. */
    suspend fun addPoints(userId: String, points: Int, reason: PointsReason, eventTitle: String? = null): PointsChange?

    /** Agrega la insignia. Devuelve false si el usuario ya la tenía o no existe. */
    suspend fun addBadge(userId: String, badge: Badge): Boolean
}
