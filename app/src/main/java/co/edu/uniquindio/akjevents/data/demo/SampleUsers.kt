package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserLevel

/** Usuarios locales de demostración. Se reemplazarán por un repositorio remoto. */
object SampleUsers {
    /** Usuario con la sesión simulada; aún no hay autenticación real. */
    val currentUser = User(
        id = "user-1",
        name = "Camilo Rodríguez",
        email = "camilo.rodriguez@akjevents.co",
        city = "Armenia, Quindío",
        address = "Cra. 14 # 21-15, Centro",
        location = Location(4.5389, -75.6725),
        points = 10
    )

    val organizers: List<User> = listOf(
        organizer("organizer-1", "Carlos Mario Gómez", UserLevel.COMMUNITY_LEADER, 1250),
        organizer("organizer-2", "Corporación Cultural del Quindío", UserLevel.ORGANIZER, 620),
        organizer("organizer-3", "Colectivo Armenia Verde", UserLevel.ORGANIZER, 540),
        organizer("organizer-4", "Laura Restrepo", UserLevel.PARTICIPANT, 180),
        organizer("organizer-5", "Junta de Acción Comunal Centro", UserLevel.ORGANIZER, 410),
        organizer("organizer-6", "Andrés Valencia", UserLevel.PARTICIPANT, 90),
        organizer("organizer-7", "Mariana Ospina", UserLevel.SPECTATOR, 20)
    )

    fun findById(id: String): User? = organizers.find { it.id == id }

    private fun organizer(id: String, name: String, level: UserLevel, points: Int) = User(
        id = id,
        name = name,
        email = "$id@akjevents.co",
        city = "Armenia, Quindío",
        address = "Armenia, Quindío",
        location = Location(4.5339, -75.6811),
        points = points,
        level = level
    )
}
