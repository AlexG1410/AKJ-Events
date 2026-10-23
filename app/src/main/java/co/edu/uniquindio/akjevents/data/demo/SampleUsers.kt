package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserRole
import java.time.LocalDateTime

/** Usuarios locales de demostración. Se reemplazarán por un repositorio remoto. */
object SampleUsers {
    /** Usuario de la cuenta de demostración (ver [DemoCredentials]). */
    val demoUser = User(
        id = "user-1",
        name = "Camilo Rodríguez",
        email = "camilo.rodriguez@akjevents.co",
        city = "Armenia, Quindío",
        address = "Cra. 14 # 21-15, Centro",
        location = Location(4.5389, -75.6725),
        points = 480,
        interests = setOf(EventCategory.SPORTS, EventCategory.CULTURE, EventCategory.VOLUNTEERING),
        badges = setOf(Badge.FIRST_EVENT, Badge.FIRST_COMMENT)
    )

    /** Moderadores precargados: no se pueden crear desde el registro. */
    val moderators: List<User> = listOf(
        User(
            id = "moderator-1",
            name = "Valentina Ríos",
            email = "moderador@akjevents.co",
            city = "Armenia, Quindío",
            address = "Nodo Armenia",
            location = Location(4.5339, -75.6811),
            role = UserRole.MODERATOR
        ),
        User(
            id = "moderator-2",
            name = "Julián Cardona",
            email = "moderacion@akjevents.co",
            city = "Calarcá, Quindío",
            address = "Nodo Calarcá",
            location = Location(4.5296, -75.6431),
            role = UserRole.MODERATOR
        )
    )

    val organizers: List<User> = listOf(
        organizer("organizer-1", "Carlos Mario Gómez", 1250),
        organizer("organizer-2", "Corporación Cultural del Quindío", 420),
        organizer("organizer-3", "Colectivo Armenia Verde", 380),
        organizer("organizer-4", "Laura Restrepo", 180),
        organizer("organizer-5", "Junta de Acción Comunal Centro", 410),
        organizer("organizer-6", "Andrés Valencia", 120),
        organizer("organizer-7", "Mariana Ospina", 20)
    )

    val all: List<User> = listOf(demoUser) + moderators + organizers

    /** Contraseñas de demostración por correo; solo existen en memoria y no tienen valor de seguridad. */
    val passwords: Map<String, String> = buildMap {
        put(demoUser.email, DemoCredentials.password)
        moderators.forEach { put(it.email, DemoCredentials.moderatorPassword) }
    }

    fun findById(id: String): User? = all.find { it.id == id }

    /** Movimientos que explican los 480 puntos iniciales del usuario de demostración. */
    fun pointsHistory(now: LocalDateTime = LocalDateTime.now()): List<PointsEntry> = listOf(
        entry(385, "Reputación acumulada en la comunidad", now.minusDays(40)),
        entry(10, "Creaste «Jornada de Reforestación Río Quindío»", now.minusDays(25)),
        entry(20, "«Jornada de Reforestación Río Quindío» fue verificado", now.minusDays(24)),
        entry(10, "«Jornada de Reforestación Río Quindío» finalizó con éxito", now.minusDays(12)),
        entry(10, "Creaste «Cine foro al parque»", now.minusDays(6)),
        entry(20, "«Cine foro al parque» fue verificado", now.minusDays(2)),
        entry(5, "Comentaste en «Ciclopaseo nocturno por Armenia»", now.minusDays(1)),
        entry(10, "Creaste «Torneo Nocturno de Voleibol Callejero»", now.minusDays(3)),
        entry(10, "Creaste «Taller de Huertos Urbanos y Compostaje»", now.minusHours(2))
    ).sortedByDescending { it.createdAt }

    private fun entry(points: Int, reason: String, at: LocalDateTime) = PointsEntry(demoUser.id, points, reason, at)

    private fun organizer(id: String, name: String, points: Int) = User(
        id = id,
        name = name,
        email = "$id@akjevents.co",
        city = "Armenia, Quindío",
        address = "Armenia, Quindío",
        location = Location(4.5339, -75.6811),
        points = points
    )
}
