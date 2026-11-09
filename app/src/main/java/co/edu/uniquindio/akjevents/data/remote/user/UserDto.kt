package co.edu.uniquindio.akjevents.data.remote.user

import androidx.annotation.Keep
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserRole

/**
 * Cómo se guarda un usuario en la colección `users` de Firestore (guía 19). Todos los campos tienen valor
 * por defecto porque `toObject()` necesita un constructor vacío. No guarda la contraseña: la maneja
 * Firebase Authentication (guía 20). El id del documento es el UID de Authentication.
 */
@Keep // Firestore llena esta clase buscando sus atributos por nombre (guía 21)
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val role: String = UserRole.USER.name,
    val points: Int = 0,
    val interests: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val profilePictureUrl: String? = null
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        city = city,
        address = address,
        location = Location(latitude, longitude),
        // Un valor desconocido no rompe la app: se toma el valor seguro
        role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER),
        points = points,
        interests = interests.mapNotNull { runCatching { EventCategory.valueOf(it) }.getOrNull() }.toSet(),
        badges = badges.mapNotNull { runCatching { Badge.valueOf(it) }.getOrNull() }.toSet(),
        profilePictureUrl = profilePictureUrl
    )

    companion object {
        fun fromDomain(user: User) = UserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            city = user.city,
            address = user.address,
            latitude = user.location.latitude,
            longitude = user.location.longitude,
            role = user.role.name,
            points = user.points,
            interests = user.interests.map { it.name },
            badges = user.badges.map { it.name },
            profilePictureUrl = user.profilePictureUrl
        )
    }
}
