package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInResult
import co.edu.uniquindio.akjevents.features.checkin.CheckInTab
import co.edu.uniquindio.akjevents.features.checkin.CheckInViewModel
import kotlinx.coroutines.test.runTest
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

    private fun checkInViewModel(eventId: String?, showMyQr: Boolean, source: TestRepositories = repos) = source.useCases.let {
        CheckInViewModel(eventId, showMyQr, it.observeCurrentUser, it.observePublicEvents, it.observeAttendances, it.getPublicEventById, it.getUserById, it.checkIn, TestResourceProvider)
    }

    @Test
    fun ownEventWithLegacyAttendanceIsNotOfferedForCheckIn() = runTest {
        val event = repos.events.findPublicEvent("feria-cultural")!!
        val legacy = TestRepositories(initialEvents = repos.events.events.value.map {
            if (it.id == event.id) it.copy(organizerId = DEMO_USER) else it
        })
        assertTrue(checkInViewModel(null, false, legacy).uiState.value.pendingEvents.isEmpty())
    }

    @Test
    fun confirmedEventsWaitForTheirCheckIn() = runTest {
        val viewModel = checkInViewModel(null, false)

        // El usuario de demostración tiene asistencia confirmada a la feria
        assertEquals(listOf("feria-cultural"), viewModel.uiState.value.pendingEvents.map { it.id })
    }

    @Test
    fun scanningAConfirmedEventRegistersTheAttendance() = runTest {
        val viewModel = checkInViewModel(null, false)
        val points = repos.pointsOf(DEMO_USER)

        viewModel.scanEvent(viewModel.uiState.value.pendingEvents.single())

        val state = viewModel.uiState.value
        assertTrue(state.result is CheckInResult.Success)
        assertEquals(points, state.pointsBefore)
        assertEquals(points + ReputationPoints.CHECK_IN, repos.pointsOf(DEMO_USER))
        assertTrue(state.pendingEvents.isEmpty())
    }

    @Test
    fun invalidCodeShowsAnError() = runTest {
        val viewModel = checkInViewModel(null, false)

        viewModel.validate()
        assertEquals("Escribe o escanea el código del evento", viewModel.uiState.value.errorMessage)

        viewModel.onCodeChange("akj-zzzzzz")
        viewModel.validate()

        assertNull(viewModel.uiState.value.result)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aPreselectedEventFillsItsCodeAndMyQrCanOpenFirst() = runTest {
        val code = repos.events.findEvent("ciclopaseo")!!.checkInCode

        assertEquals(code, checkInViewModel("ciclopaseo", false).uiState.value.code)
        assertEquals(CheckInTab.MY_QR, checkInViewModel(null, true).uiState.value.tab)
    }
}
