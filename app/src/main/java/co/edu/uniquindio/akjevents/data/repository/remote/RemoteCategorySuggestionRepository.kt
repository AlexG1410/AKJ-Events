package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.remote.ai.AiServiceConfig
import co.edu.uniquindio.akjevents.data.remote.ai.AiSessionTokenProvider
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionApi
import co.edu.uniquindio.akjevents.data.remote.ai.CategorySuggestionRequest
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionException
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionFailure
import co.edu.uniquindio.akjevents.domain.repository.CategorySuggestionRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException

class RemoteCategorySuggestionRepository @Inject constructor(
    private val api: CategorySuggestionApi,
    private val tokens: AiSessionTokenProvider,
    private val config: AiServiceConfig
) : CategorySuggestionRepository {
    override suspend fun suggest(title: String, description: String): EventCategory {
        if (!config.isConfigured) throw CategorySuggestionException(CategorySuggestionFailure.NOT_CONFIGURED)
        try {
            return withTimeoutOrNull(22_000) {
                val response = api.suggest("Bearer ${tokens.token()}", CategorySuggestionRequest(title, description))
                EventCategory.entries.firstOrNull { it.name == response.category }
                    ?: throw CategorySuggestionException(CategorySuggestionFailure.INVALID_RESPONSE)
            } ?: throw CategorySuggestionException(CategorySuggestionFailure.UNAVAILABLE)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: CategorySuggestionException) {
            throw failure
        } catch (error: HttpException) {
            throw CategorySuggestionException(when (error.code()) {
                401, 403 -> CategorySuggestionFailure.SESSION
                429 -> CategorySuggestionFailure.RATE_LIMIT
                else -> CategorySuggestionFailure.UNAVAILABLE
            })
        } catch (_: Exception) {
            // No filtrar mensajes HTTP, datos del proveedor ni tokens hacia la interfaz o Logcat.
            throw CategorySuggestionException(CategorySuggestionFailure.UNAVAILABLE)
        }
    }
}
