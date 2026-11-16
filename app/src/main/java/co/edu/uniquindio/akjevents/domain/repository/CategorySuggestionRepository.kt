package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.EventCategory

/** Solo título y descripción; no envía perfil, ubicación, fotos ni historial. */
fun interface CategorySuggestionRepository {
    suspend fun suggest(title: String, description: String): EventCategory
}

enum class CategorySuggestionFailure { NOT_CONFIGURED, SESSION, RATE_LIMIT, UNAVAILABLE, INVALID_RESPONSE }

class CategorySuggestionException(val failure: CategorySuggestionFailure) : Exception(failure.name)
