package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

/** Contexto breve: no incluye nombre, correo, dirección ni coordenadas del usuario. */
data class RecommendationEvent(val id: String, val title: String, val description: String, val category: EventCategory)

data class RecommendationInteraction(val event: RecommendationEvent, val checkedIn: Boolean)

data class RecommendationCandidate(
    val event: RecommendationEvent,
    val startsAt: LocalDateTime,
    val distanceKm: Double,
    val remainingSpots: Int?,
    val availableRatio: Double
)

data class RecommendationRequest(
    val interests: Set<EventCategory>,
    val interactions: List<RecommendationInteraction>,
    val candidates: List<RecommendationCandidate>
)

/** Contrato de salida del clasificador: solo referencias a los candidatos recibidos. */
data class RankedRecommendation(val eventId: String, val reason: String)

data class RecommendedEvent(val event: CommunityEvent, val reason: String, val distanceKm: Double)

data class RecommendationFeed(
    val userId: String? = null,
    val items: List<RecommendedEvent> = emptyList(),
    val hasPersonalization: Boolean = false
)
