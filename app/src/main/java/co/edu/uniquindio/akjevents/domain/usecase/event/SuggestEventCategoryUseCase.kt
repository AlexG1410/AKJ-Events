package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionRepository
import javax.inject.Inject

class SuggestEventCategoryUseCase @Inject constructor(
    private val repository: CategorySuggestionRepository
) {
    suspend operator fun invoke(title: String, description: String) = repository.suggest(
        title.trim().also { require(it.length in MIN_TITLE_LENGTH..MAX_TITLE_LENGTH) },
        description.trim().also { require(it.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) }
    )

    companion object {
        const val MIN_TITLE_LENGTH = 5
        const val MAX_TITLE_LENGTH = 80
        const val MIN_DESCRIPTION_LENGTH = 20
        const val MAX_DESCRIPTION_LENGTH = 600
    }
}
