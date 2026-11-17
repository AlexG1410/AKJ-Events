package co.edu.uniquindio.akjevents

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.features.profile.edit.DeleteAccountDialog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DeleteAccountDialogSmokeTest {
    @get:Rule val compose = createComposeRule()
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test fun cancelNeverSubmitsCredentialsOrDeletion() {
        var submitted = 0
        var dismissed = 0
        compose.setContent { AKJEventsTheme { DeleteAccountDialog({ submitted++ }, { dismissed++ }) } }
        compose.onNodeWithText(text(R.string.edit_profile_delete_confirm)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.field_password)).performTextInput("ephemeral-test-password")
        compose.onNodeWithText(text(R.string.action_cancel)).performClick()
        assertEquals(0, submitted)
        assertEquals(1, dismissed)
    }
}
