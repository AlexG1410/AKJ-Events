package co.edu.uniquindio.akjevents.data.demo

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import java.time.LocalDateTime

/** Datos locales de demostración. Se reemplazarán por un repositorio remoto. */
object SampleEvents {
    private val now = LocalDateTime.now()
    private val tomorrow = now.plusDays(1)

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
            id = "cine-foro",
            organizerId = "user-1",
            title = "Cine foro al parque",
            category = EventCategory.CULTURE,
            description = "Proyección al aire libre de cine colombiano y conversación con la comunidad. Trae tu cobija o silla plegable.",
            address = "Biblioteca Pública Municipal, Armenia, Quindío",
            location = Location(4.5356, -75.6762),
            imageUrls = listOf("https://picsum.photos/seed/akj-cine/900/500"),
            startsAt = tomorrow.plusDays(2).withHour(18).withMinute(30),
            endsAt = tomorrow.plusDays(2).withHour(21).withMinute(0),
            capacity = 60,
            attendanceCount = 18,
            interestCount = 22,
            status = EventStatus.VERIFIED,
            createdAt = now.minusDays(6)
        ),
        CommunityEvent(
            id = "caminata-cocora",
            organizerId = "organizer-3",
            title = "Caminata ecológica Valle de Cocora",
            category = EventCategory.SPORTS,
            description = "Recorrido guiado de 12 km entre palmas de cera. Nivel intermedio; lleva botas, impermeable y refrigerio.",
            address = "Valle de Cocora, Salento, Quindío",
            location = Location(4.6380, -75.4870),
            imageUrls = listOf("https://picsum.photos/seed/akj-cocora/900/500"),
            startsAt = tomorrow.plusDays(9).withHour(6).withMinute(30),
            endsAt = tomorrow.plusDays(9).withHour(13).withMinute(0),
            capacity = 40,
            attendanceCount = 27,
            interestCount = 44,
            status = EventStatus.VERIFIED
        ),
        CommunityEvent(
            id = "hackaton-utp",
            organizerId = "organizer-4",
            title = "Hackatón universitario de soluciones cívicas",
            category = EventCategory.ACADEMIC,
            description = "24 horas para crear soluciones tecnológicas para la región. Equipos de 3 a 5 personas; hay mentorías y comida.",
            address = "Universidad Tecnológica de Pereira, Pereira, Risaralda",
            location = Location(4.7946, -75.6900),
            imageUrls = listOf("https://picsum.photos/seed/akj-hackaton/900/500"),
            startsAt = tomorrow.plusDays(12).withHour(8).withMinute(0),
            endsAt = tomorrow.plusDays(13).withHour(8).withMinute(0),
            capacity = 120,
            attendanceCount = 64,
            interestCount = 58,
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
            status = EventStatus.PENDING,
            createdAt = now.minusDays(1)
        ),
        CommunityEvent(
            id = "huertos-urbanos",
            organizerId = "user-1",
            title = "Taller de Huertos Urbanos y Compostaje",
            category = EventCategory.VOLUNTEERING,
            description = "Aprenderemos a cultivar alimentos orgánicos en balcones y terrazas reducidas, a preparar compost casero " +
                "y técnicas de reciclaje de sustrato para la comunidad.",
            address = "Parque de la Vida, Armenia, Quindío",
            location = Location(4.5511, -75.6598),
            imageUrls = listOf("https://picsum.photos/seed/akj-huertos/900/500"),
            startsAt = tomorrow.plusDays(8).withHour(9).withMinute(0),
            endsAt = tomorrow.plusDays(8).withHour(12).withMinute(30),
            capacity = 35,
            status = EventStatus.PENDING,
            createdAt = now.minusHours(2)
        ),
        CommunityEvent(
            id = "microfutbol",
            organizerId = "organizer-5",
            title = "Torneo Relámpago de Microfútbol",
            category = EventCategory.SPORTS,
            description = "Torneo de un día para equipos barriales de 5 jugadores. Inscripción por equipos en la casa comunal.",
            address = "Estadio Centenario, Armenia, Quindío",
            location = Location(4.5208, -75.6858),
            imageUrls = listOf("https://picsum.photos/seed/akj-microfutbol/900/500"),
            startsAt = tomorrow.plusDays(10).withHour(14).withMinute(0),
            endsAt = tomorrow.plusDays(10).withHour(19).withMinute(0),
            capacity = 60,
            status = EventStatus.PENDING,
            createdAt = now.minusMinutes(40)
        ),
        CommunityEvent(
            id = "muestra-artesanal",
            organizerId = "organizer-2",
            title = "Muestra Artesanal y Café Quindiano",
            category = EventCategory.SOCIAL,
            description = "Artesanos y caficultores de la región muestran su trabajo. Habrá catas guiadas de café de origen.",
            address = "Plaza principal de Calarcá, Calarcá, Quindío",
            location = Location(4.5296, -75.6431),
            imageUrls = listOf("https://picsum.photos/seed/akj-artesanal/900/500"),
            startsAt = tomorrow.plusDays(14).withHour(10).withMinute(0),
            endsAt = tomorrow.plusDays(14).withHour(18).withMinute(0),
            capacity = 120,
            status = EventStatus.PENDING,
            createdAt = now.minusHours(5)
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
        ),
        CommunityEvent(
            id = "voleibol-callejero",
            organizerId = "user-1",
            title = "Torneo Nocturno de Voleibol Callejero",
            category = EventCategory.SPORTS,
            description = "Torneo abierto de voleibol 6 contra 6 en la cancha del barrio, con premiación para los tres primeros equipos.",
            address = "Cancha Barrio Granada, Armenia, Quindío",
            location = Location(4.5150, -75.6920),
            imageUrls = listOf("https://picsum.photos/seed/akj-voleibol/900/500"),
            startsAt = tomorrow.plusDays(3).withHour(19).withMinute(0),
            endsAt = tomorrow.plusDays(3).withHour(22).withMinute(0),
            status = EventStatus.REJECTED,
            rejectionReason = "Lugar no disponible o sin permiso municipal: la cancha tiene reserva prioritaria de las escuelas " +
                "deportivas los viernes por la noche. Elige otro horario o adjunta la autorización de la junta de acción comunal.",
            createdAt = now.minusDays(3)
        ),
        CommunityEvent(
            id = "reforestacion-rio",
            organizerId = "user-1",
            title = "Jornada de Reforestación Río Quindío",
            category = EventCategory.VOLUNTEERING,
            description = "Siembra de árboles nativos en la cuenca alta del río Quindío junto con la comunidad y guardabosques.",
            address = "Cuenca Alta del Río Quindío, Salento, Quindío",
            location = Location(4.6290, -75.5300),
            imageUrls = listOf("https://picsum.photos/seed/akj-reforestacion/900/500"),
            startsAt = now.minusDays(12).withHour(7).withMinute(0),
            endsAt = now.minusDays(12).withHour(13).withMinute(0),
            capacity = 50,
            attendanceCount = 45,
            interestCount = 33,
            status = EventStatus.FINISHED,
            createdAt = now.minusDays(25)
        )
    )

    fun findPublicById(id: String): CommunityEvent? =
        all.find { it.id == id && it.status == EventStatus.VERIFIED }
}
