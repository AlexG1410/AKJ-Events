package co.edu.uniquindio.akjevents

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.features.event.create.CategorySuggestion
import co.edu.uniquindio.akjevents.features.event.create.CategorySuggestionStatus
import co.edu.uniquindio.akjevents.features.event.create.CreateEventUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Solo UI con estados simulados: no obtiene tokens ni consulta Firebase/OpenRouter. */
class CategorySuggestionSmokeTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)
    private val input = CreateEventUiState(title = "Taller comunitario", description = "Aprenderemos juntos sobre huertos urbanos.")

    @Test fun suggestionIsVisibleAndCanBeAccepted() {
        var accepted = 0
        compose.setContent {
            AKJEventsTheme {
                CategorySuggestion(input.copy(suggestionStatus = CategorySuggestionStatus.READY, suggestedCategory = EventCategory.ACADEMIC), {}, { accepted++ })
            }
        }
        compose.onNodeWithText(context.getString(R.string.ai_category_result, text(R.string.category_academic))).assertExists()
        compose.onNodeWithText(text(R.string.ai_category_accept)).performClick()
        assertEquals(1, accepted)
    }

    @Test fun loadingDisablesRepeatedRequests() {
        compose.setContent {
            AKJEventsTheme { CategorySuggestion(input.copy(suggestionStatus = CategorySuggestionStatus.LOADING), {}, {}) }
        }
        compose.onNodeWithText(text(R.string.ai_category_loading)).assertExists()
        compose.onNodeWithText(text(R.string.ai_category_request)).assertIsNotEnabled()
    }

    @Test fun failureExplainsManualFallbackAndAllowsRetry() {
        var requests = 0
        compose.setContent {
            AKJEventsTheme {
                CategorySuggestion(input.copy(suggestionStatus = CategorySuggestionStatus.ERROR, suggestionError = text(R.string.ai_category_error)), { requests++ }, {})
            }
        }
        compose.onNodeWithText(text(R.string.ai_category_error)).assertExists()
        compose.onNodeWithText(text(R.string.ai_category_request)).assertIsEnabled().performClick()
        assertEquals(1, requests)
    }
}
