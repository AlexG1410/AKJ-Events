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
    val interests: Set<EventCategory> = emptySet(),
    val badges: Set<Badge> = emptySet(),
    val profilePictureUrl: String? = null
) {
    /** El nivel no se guarda: depende solo de los puntos acumulados. */
    val level: UserLevel get() = UserLevel.forPoints(points)

    val isModerator: Boolean get() = role == UserRole.MODERATOR

    val firstName: String get() = name.substringBefore(" ")

    /** "CR" para "Camilo Rodríguez". */
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
}
