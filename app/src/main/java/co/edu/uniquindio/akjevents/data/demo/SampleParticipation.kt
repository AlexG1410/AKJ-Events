package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import java.time.LocalDateTime

/** Comentarios, asistencias e intereses de demostración. Los conteos de cada evento ya los incluyen. */
object SampleParticipation {
    private val now = LocalDateTime.now()

    val comments: List<Comment> = listOf(
        Comment("comentario-1", "ciclopaseo", "organizer-4", "¿El recorrido es apto para niños de 10 años en bicicleta?", now.minusDays(2)),
        Comment("comentario-2", "ciclopaseo", "organizer-1", "¡Sí! Vamos a un ritmo suave y habrá adultos acompañando al final del grupo.", now.minusDays(2).plusHours(1)),
        Comment("comentario-3", "ciclopaseo", "user-1", "Allá estaremos con los vecinos del barrio. ¡Gracias por organizarlo!", now.minusDays(1)),
        Comment("comentario-4", "cine-foro", "organizer-5", "¿Hay que llevar silla o habrá dónde sentarse?", now.minusHours(3)),
        Comment("comentario-5", "reforestacion-rio", "organizer-3", "Gran jornada, sembramos más de 300 árboles. ¡Gracias a todos!", now.minusDays(11))
    )

    val attendances: List<Attendance> = listOf(
        Attendance("feria-cultural", "user-1"),
        Attendance("cine-foro", "organizer-4"),
        Attendance("cine-foro", "organizer-7"),
        Attendance("reforestacion-rio", "organizer-3", checkedIn = true),
        Attendance("reforestacion-rio", "organizer-6", checkedIn = true)
    )

    val interests: List<Interest> = listOf(
        Interest("jornada-verde", "user-1"),
        Interest("cine-foro", "organizer-4")
    )

    val notifications: List<AppNotification> = listOf(
        AppNotification(
            id = "notificacion-1",
            userId = "user-1",
            type = NotificationType.NEW_COMMENT,
            title = "Nueva pregunta en tu evento",
            message = "Junta de Acción Comunal Centro comentó en «Cine foro al parque»: «¿Hay que llevar silla o habrá dónde sentarse?»",
            createdAt = now.minusHours(3),
            eventId = "cine-foro"
        ),
        AppNotification(
            id = "notificacion-2",
            userId = "user-1",
            type = NotificationType.REMINDER,
            title = "Recordatorio: mañana es el Ciclopaseo nocturno",
            message = "Recuerda tener a mano tu código QR para el check-in rápido en la Plaza de Bolívar a las 7:00 PM.",
            createdAt = now.minusHours(1),
            eventId = "ciclopaseo"
        ),
        AppNotification(
            id = "notificacion-3",
            userId = "user-1",
            type = NotificationType.EVENT_REJECTED,
            title = "Revisión requerida: Torneo Nocturno de Voleibol Callejero",
            message = "Motivo: Lugar no disponible o sin permiso municipal. Ajusta el evento y envíalo de nuevo.",
            createdAt = now.minusDays(1),
            eventId = "voleibol-callejero"
        ),
        AppNotification(
            id = "notificacion-4",
            userId = "user-1",
            type = NotificationType.EVENT_VERIFIED,
            title = "¡Tu evento ha sido aprobado y publicado!",
            message = "«Cine foro al parque» superó la revisión y ya es visible para la comunidad.",
            createdAt = now.minusDays(2),
            eventId = "cine-foro",
            isRead = true
        ),
        AppNotification(
            id = "notificacion-5",
            userId = "user-1",
            type = NotificationType.BADGE_UNLOCKED,
            title = "Nueva insignia: Primer aporte",
            message = "Comentaste por primera vez en un evento. ¡Sigue participando!",
            createdAt = now.minusDays(9),
            isRead = true
        )
    )
}
