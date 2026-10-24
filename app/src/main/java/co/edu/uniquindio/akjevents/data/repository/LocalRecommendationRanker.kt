package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.RankedRecommendation
import co.edu.uniquindio.akjevents.domain.model.RecommendationCandidate
import co.edu.uniquindio.akjevents.domain.model.RecommendationEvent
import co.edu.uniquindio.akjevents.domain.model.RecommendationRequest
import co.edu.uniquindio.akjevents.domain.repository.RecommendationRanker

/** Reglas locales, no IA: afinidad de categorías/texto, cercanía, cupos y diversidad. */
class LocalRecommendationRanker : RecommendationRanker {
    override suspend fun rank(request: RecommendationRequest): List<RankedRecommendation> {
        val historyCategories = request.interactions.groupBy { it.event.category }
        val historyWords = request.interactions.flatMap { words(it.event) }.toSet()
        val remaining = request.candidates.distinctBy { it.event.id }.toMutableList()
        val selectedCategories = mutableMapOf<EventCategory, Int>()
        val result = mutableListOf<RankedRecommendation>()

        fun score(candidate: RecommendationCandidate): Double {
            val category = candidate.event.category
            val participation = historyCategories[category].orEmpty().sumOf { if (it.checkedIn) 12 else 8 }.coerceAtMost(30)
            val overlap = words(candidate.event).intersect(historyWords).size.coerceAtMost(5) * 4
            val proximity = 15.0 / (1.0 + candidate.distanceKm / 5.0)
            val diversityPenalty = (selectedCategories[category] ?: 0) * 12
            return (if (category in request.interests) 40 else 0) + participation + overlap +
                proximity + candidate.availableRatio * 5 - diversityPenalty
        }

        while (remaining.isNotEmpty()) {
            val next = remaining.sortedWith(
                compareByDescending<RecommendationCandidate> { score(it) }
                    .thenBy { it.startsAt }.thenBy { it.event.id }
            ).first()
            val category = next.event.category
            val reason = when {
                category in request.interests -> "Coincide con tu interés en ${category.label.lowercase()}."
                historyCategories[category].orEmpty().any { it.checkedIn } -> "Similar a categorías de eventos en los que hiciste check-in."
                historyCategories[category].orEmpty().isNotEmpty() -> "De una categoría que marcaste «Me interesa»."
                words(next.event).intersect(historyWords).isNotEmpty() -> "Comparte temas con eventos que te interesaron o en los que participaste."
                next.distanceKm <= 5 -> "Una opción cercana para descubrir en tu comunidad."
                else -> "Una opción para descubrir nuevas actividades con cupos disponibles."
            }
            result += RankedRecommendation(next.event.id, reason)
            selectedCategories[category] = (selectedCategories[category] ?: 0) + 1
            remaining.remove(next)
        }
        return result
    }

    private fun words(event: RecommendationEvent): Set<String> =
        "${event.title} ${event.description} ${event.category.label}".normalizedForSearch()
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 4 && it !in STOP_WORDS }.toSet()

    private companion object {
        val STOP_WORDS = setOf("para", "como", "este", "esta", "estos", "estas", "desde", "sobre", "entre", "evento", "eventos", "actividad", "actividades", "comunidad")
    }
}
