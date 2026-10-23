package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.CheckInResult
import co.edu.uniquindio.akjevents.features.checkin.CheckInTab
import co.edu.uniquindio.akjevents.features.checkin.CheckInViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CheckInViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories()

    @Test
    fun confirmedEventsWaitForTheirCheckIn() {
        val viewModel = CheckInViewModel(null, false, repos.events, repos.users)

        // El usuario de demostración tiene asistencia confirmada a la feria
        assertEquals(listOf("feria-cultural"), viewModel.uiState.value.pendingEvents.map { it.id })
    }

    @Test
    fun scanningAConfirmedEventRegistersTheAttendance() {
        val viewModel = CheckInViewModel(null, false, repos.events, repos.users)
        val points = repos.pointsOf(DEMO_USER)

        viewModel.scanEvent(viewModel.uiState.value.pendingEvents.single())

        val state = viewModel.uiState.value
        assertTrue(state.result is CheckInResult.Success)
        assertEquals(points, state.pointsBefore)
        assertEquals(points + ReputationPoints.CHECK_IN, repos.pointsOf(DEMO_USER))
        assertTrue(state.pendingEvents.isEmpty())
    }

    @Test
    fun invalidCodeShowsAnError() {
        val viewModel = CheckInViewModel(null, false, repos.events, repos.users)

        viewModel.validate()
        assertEquals("Escribe o escanea el código del evento", viewModel.uiState.value.errorMessage)

        viewModel.onCodeChange("akj-zzzzzz")
        viewModel.validate()

        assertNull(viewModel.uiState.value.result)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aPreselectedEventFillsItsCodeAndMyQrCanOpenFirst() {
        val code = repos.events.findEvent("ciclopaseo")!!.checkInCode

        assertEquals(code, CheckInViewModel("ciclopaseo", false, repos.events, repos.users).uiState.value.code)
        assertEquals(CheckInTab.MY_QR, CheckInViewModel(null, true, repos.events, repos.users).uiState.value.tab)
    }
}
