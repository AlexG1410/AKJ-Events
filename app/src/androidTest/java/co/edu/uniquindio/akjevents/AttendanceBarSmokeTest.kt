package co.edu.uniquindio.akjevents

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.features.event.detail.AttendanceBar
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AttendanceBarSmokeTest {
    @get:Rule val compose = createComposeRule()
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test fun afterCheckInShowsDisabledStatusInsteadOfCancellation() {
        var calls = 0
        compose.setContent { AKJEventsTheme { AttendanceBar(false, true, true, {}, { calls++ }) } }
        compose.onNodeWithText(text(R.string.detail_cancel_attendance)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.detail_checked_in), substring = true).assertIsNotEnabled().performClick()
        assertEquals(0, calls)
    }

    @Test fun beforeCheckInCancellationRemainsEnabled() {
        var calls = 0
        compose.setContent { AKJEventsTheme { AttendanceBar(false, true, false, {}, { calls++ }) } }
        compose.onNodeWithText(text(R.string.detail_cancel_attendance), substring = true).assertIsEnabled().performClick()
        assertEquals(1, calls)
    }
}
