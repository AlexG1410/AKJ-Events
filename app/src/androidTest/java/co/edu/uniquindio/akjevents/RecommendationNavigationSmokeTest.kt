package co.edu.uniquindio.akjevents

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.core.util.ResourceProviderImpl
import co.edu.uniquindio.akjevents.data.remote.ai.*
import co.edu.uniquindio.akjevents.data.repository.*
import co.edu.uniquindio.akjevents.data.repository.remote.RemoteRecommendationRanker
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.domain.usecase.recommendation.*
import co.edu.uniquindio.akjevents.features.recommendations.*
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** API simulada contada; prueba navegación de los componentes reales, no tráfico en producción. */
class RecommendationNavigationSmokeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun previewAndViewAllReuseOneRemoteClassification() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val calls = AtomicInteger()
        val user = User("reader", "Prueba", "reader@example.test", "Ciudad", "Lugar", Location(4.5, -75.6))
        val users = InMemoryUserRepository(listOf(user), sessionUserId = user.id)
        val now = LocalDateTime.now()
        val event = CommunityEvent("event", "organizer", "Evento de prueba", EventCategory.CULTURE, "Descripción", "Lugar", user.location,
            listOf("file:///nonexistent.png"), now.plusDays(1), now.plusDays(1).plusHours(2), status = EventStatus.VERIFIED)
        val events = InMemoryEventRepository(listOf(event), emptyList(), emptyList(), emptyList())
        val api = object : RecommendationApi {
            override suspend fun recommend(authorization: String, request: RecommendationRequestDto): RecommendationResponseDto {
                calls.incrementAndGet()
                return RecommendationResponseDto(request.candidates.map { RankedRecommendationDto(it.eventId, "Motivo de prueba") })
            }
        }
        val repo = LocalRecommendationRepository(events, users, ResourceProviderImpl(context), RemoteRecommendationRanker(api, { "fake-token" }, AiServiceConfig("https://worker.test/")))
        lateinit var vm: RecommendationsViewModel
        compose.runOnUiThread { vm = RecommendationsViewModel(ObserveRecommendationChangesUseCase(repo), GetRecommendationsUseCase(repo), RevalidateRecommendationsUseCase(repo), { now }) }
        compose.setContent {
            var all by remember { mutableStateOf(false) }
            AKJEventsTheme {
                if (all) RecommendationsScreen({ all = false }, {}, vm)
                else RecommendationsPreview(vm, { all = true }, {})
            }
        }
        compose.onNodeWithText(context.getString(R.string.recommendations_source_remote)).assertExists()
        assertEquals(1, calls.get())
        compose.onNodeWithText(context.getString(R.string.action_see_all)).performClick()
        compose.onNodeWithText(context.getString(R.string.recommendations_source_remote)).assertExists()
        compose.onNodeWithText("Evento de prueba").assertExists()
        assertEquals(1, calls.get())
    }
}
