package co.edu.uniquindio.akjevents.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val city: String,
    val address: String,
    val location: Location,
    val role: UserRole = UserRole.USER,
    val points: Int = 0,
    val level: UserLevel = UserLevel.SPECTATOR,
    val profilePictureUrl: String? = null
)
