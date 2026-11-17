package co.edu.uniquindio.akjevents.data.remote.ai

import androidx.annotation.Keep
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Keep
data class CategorySuggestionRequest(val title: String, val description: String)

@Keep
data class CategorySuggestionResponse(val category: String? = null)

interface CategorySuggestionApi {
    @POST("suggest-category")
    suspend fun suggest(
        @Header("Authorization") authorization: String,
        @Body request: CategorySuggestionRequest
    ): CategorySuggestionResponse
}
