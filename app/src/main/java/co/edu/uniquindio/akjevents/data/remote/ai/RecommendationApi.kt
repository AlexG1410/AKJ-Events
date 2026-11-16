package co.edu.uniquindio.akjevents.data.remote.ai

import androidx.annotation.Keep
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Keep
data class RecommendationCandidateDto(val eventId: String, val title: String, val description: String, val category: String)

@Keep
data class RecommendationInteractionDto(val title: String, val description: String, val category: String, val checkedIn: Boolean)

@Keep
data class RecommendationRequestDto(
    val interests: List<String>, val interactions: List<RecommendationInteractionDto>,
    val candidates: List<RecommendationCandidateDto>, val language: String
)

@Keep
data class RankedRecommendationDto(val eventId: String? = null, val reason: String? = null)

@Keep
data class RecommendationResponseDto(val items: List<RankedRecommendationDto?>? = null)

interface RecommendationApi {
    @POST("recommend-events")
    suspend fun recommend(@Header("Authorization") authorization: String, @Body request: RecommendationRequestDto): RecommendationResponseDto
}
