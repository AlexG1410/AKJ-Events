package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.*
import java.time.LocalDateTime

internal val recommendationNow: LocalDateTime = LocalDateTime.of(2026, 10, 23, 12, 0)
internal val recommendationLocation = Location(4.5339, -75.6811)

internal fun recommendationEvent(
    id: String,
    category: EventCategory = EventCategory.SPORTS,
    title: String = id,
    description: String = "Una actividad para descubrir",
    status: EventStatus = EventStatus.VERIFIED,
    start: LocalDateTime = recommendationNow.plusDays(1),
    owner: String = "organizador",
    capacity: Int? = 30,
    attendance: Int = 0,
    location: Location = recommendationLocation
) = CommunityEvent(
    id = id, organizerId = owner, title = title, category = category, description = description,
    address = "Lugar del evento", location = location, imageUrls = listOf("imagen-local"),
    startsAt = start, endsAt = start.plusHours(2), capacity = capacity, attendanceCount = attendance,
    status = status, rejectionReason = if (status == EventStatus.REJECTED) "No procede" else null
)

internal class RecommendationFixture(
    initialEvents: List<CommunityEvent>,
    interests: Set<EventCategory> = emptySet(),
    liked: List<Interest> = emptyList(),
    attendances: List<Attendance> = emptyList()
) {
    val user = User("lector", "Ana", "ana@ejemplo.com", "Armenia, Quindío", "Dirección privada", recommendationLocation, interests = interests)
    val users = InMemoryUserRepository(
        initialUsers = listOf(user), initialPasswords = mapOf(user.email to "Comunidad1"),
        initialPointsHistory = emptyList(), sessionUserId = user.id
    )
    val events = InMemoryEventRepository(
        initialEvents = initialEvents, initialComments = emptyList(), initialAttendances = attendances,
        initialInterests = liked, users = users
    )
}
