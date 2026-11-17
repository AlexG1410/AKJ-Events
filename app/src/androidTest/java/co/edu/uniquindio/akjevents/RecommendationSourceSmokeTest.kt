package co.edu.uniquindio.akjevents

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.domain.model.RecommendationSource
import co.edu.uniquindio.akjevents.features.recommendations.RecommendationSourceLabel
import org.junit.Rule
import org.junit.Test

/** Estados simulados; no inicia sesión ni consume cuota de IA. */
class RecommendationSourceSmokeTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun remoteSourceHasAnExplicitLabel() {
        compose.setContent { AKJEventsTheme { RecommendationSourceLabel(RecommendationSource.REMOTE) } }
        compose.onNodeWithText(context.getString(R.string.recommendations_source_remote)).assertExists()
    }

    @Test fun fallbackIsNotPresentedAsRemoteAi() {
        compose.setContent { AKJEventsTheme { RecommendationSourceLabel(RecommendationSource.LOCAL) } }
        compose.onNodeWithText(context.getString(R.string.recommendations_source_local)).assertExists()
        compose.onNodeWithText(context.getString(R.string.recommendations_source_remote)).assertDoesNotExist()
    }
}
