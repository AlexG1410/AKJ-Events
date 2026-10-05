package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import java.time.LocalDateTime

/** Datos locales de demostración. Se reemplazarán por un repositorio remoto. */
object SampleEvents {
    private val tomorrow = LocalDateTime.now().plusDays(1)

    val all: List<CommunityEvent> = listOf(
        CommunityEvent(
            id = "ciclopaseo",
            organizerId = "organizer-1",
            title = "Ciclopaseo nocturno por Armenia",
            category = EventCategory.SPORTS,
            description = "Recorre la ciudad en comunidad. Trae bicicleta, casco y luces. Habrá puntos de hidratación durante el recorrido.",
            address = "Plaza de Bolívar, Armenia, Quindío",
            location = Location(4.5339, -75.6811),
            imageUrls = listOf("https://picsum.photos/seed/akj-ciclopaseo/900/500"),
            startsAt = tomorrow.withHour(19).withMinute(0),
            endsAt = tomorrow.withHour(21).withMinute(30),
            capacity = 100,
            attendanceCount = 84,
            interestCount = 39,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "feria-cultural",
            organizerId = "organizer-2",
            title = "Feria cultural y de emprendimiento",
            category = EventCategory.CULTURE,
            description = "Conoce artistas, sabores y emprendimientos locales en una jornada abierta para toda la familia.",
            address = "Parque Sucre, Armenia, Quindío",
            location = Location(4.5382, -75.6731),
            imageUrls = listOf("https://picsum.photos/seed/akj-feria/900/500"),
            startsAt = tomorrow.plusDays(2).withHour(9).withMinute(0),
            endsAt = tomorrow.plusDays(2).withHour(16).withMinute(0),
            attendanceCount = 142,
            interestCount = 71,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "jornada-verde",
            organizerId = "organizer-3",
            title = "Jornada de limpieza Parque de la Vida",
            category = EventCategory.VOLUNTEERING,
            description = "Ayúdanos a cuidar el parque con vecinos y voluntarios. Lleva ropa cómoda y una botella de agua.",
            address = "Parque de la Vida, Armenia, Quindío",
            location = Location(4.5511, -75.6598),
            imageUrls = listOf("https://picsum.photos/seed/akj-voluntariado/900/500"),
            startsAt = tomorrow.plusDays(4).withHour(8).withMinute(0),
            endsAt = tomorrow.plusDays(4).withHour(11).withMinute(0),
            capacity = 50,
            attendanceCount = 38,
            interestCount = 25,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "taller-kotlin",
            organizerId = "organizer-4",
            title = "Taller introductorio de Kotlin",
            category = EventCategory.ACADEMIC,
            description = "Un espacio gratuito para aprender fundamentos de programación móvil en compañía de la comunidad.",
            address = "Centro de Innovación, Armenia, Quindío",
            location = Location(4.5320, -75.6750),
            imageUrls = listOf("https://picsum.photos/seed/akj-kotlin/900/500"),
            startsAt = tomorrow.plusDays(5).withHour(17).withMinute(0),
            endsAt = tomorrow.plusDays(5).withHour(19).withMinute(0),
            capacity = 35,
            attendanceCount = 31,
            interestCount = 31,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "encuentro-vecinal",
            organizerId = "organizer-5",
            title = "Encuentro de vecinos y café",
            category = EventCategory.SOCIAL,
            description = "Un encuentro para conocernos y conversar sobre ideas para el barrio.",
            address = "Casa comunal, Armenia, Quindío",
            location = Location(4.5410, -75.6700),
            imageUrls = listOf("https://picsum.photos/seed/akj-cafe/900/500"),
            startsAt = tomorrow.plusDays(6).withHour(16).withMinute(0),
            endsAt = tomorrow.plusDays(6).withHour(18).withMinute(0),
            capacity = 25,
            attendanceCount = 25,
            interestCount = 8,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "pendiente-privado",
            organizerId = "organizer-6",
            title = "Evento en revisión",
            category = EventCategory.SOCIAL,
            description = "Este ejemplo sirve para comprobar que un evento pendiente no aparezca en el feed público.",
            address = "Armenia, Quindío",
            location = Location(4.5339, -75.6811),
            imageUrls = listOf("https://picsum.photos/seed/akj-pendiente/900/500"),
            startsAt = tomorrow.plusDays(7).withHour(10).withMinute(0),
            endsAt = tomorrow.plusDays(7).withHour(12).withMinute(0),
            status = EventStatus.PENDING
        ),
        CommunityEvent(
            id = "rechazado-privado",
            organizerId = "organizer-7",
            title = "Evento no publicado",
            category = EventCategory.SPORTS,
            description = "Este evento rechazado tampoco debe ser visible en el feed público.",
            address = "Armenia, Quindío",
            location = Location(4.5339, -75.6811),
            imageUrls = listOf("https://picsum.photos/seed/akj-rechazado/900/500"),
            startsAt = tomorrow.plusDays(8).withHour(10).withMinute(0),
            endsAt = tomorrow.plusDays(8).withHour(12).withMinute(0),
            status = EventStatus.REJECTED,
            rejectionReason = "Falta información sobre el lugar del evento"
        )
    )

    fun findPublicById(id: String): CommunityEvent? =
        all.find { it.id == id && it.status == EventStatus.VERIFIED }
}
