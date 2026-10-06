package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.features.event.detail.EventDetailViewModel
import co.edu.uniquindio.akjevents.features.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EventDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = InMemoryEventRepository()

    private fun detail(id: String) = EventDetailViewModel(id, repository)

    @Test
    fun loadsVerifiedEventWithItsOrganizer() {
        val state = detail("ciclopaseo").uiState.value

        assertEquals("ciclopaseo", state.event?.id)
        assertEquals("organizer-1", state.organizer?.id)
    }

    @Test
    fun pendingAndRejectedEventsAreNotAvailable() {
        assertNull(detail("pendiente-privado").uiState.value.event)
        assertNull(detail("rechazado-privado").uiState.value.event)
        assertNull(detail("no-existe").uiState.value.event)
    }

    @Test
    fun confirmingAttendanceAddsOneAttendee() {
        val viewModel = detail("ciclopaseo")
        val before = viewModel.uiState.value.event!!.attendanceCount

        viewModel.toggleAttendance()

        val state = viewModel.uiState.value
        assertEquals(before + 1, state.event!!.attendanceCount)
        assertTrue(state.isAttending)
        assertNotNull(state.userMessage)
    }

    @Test
    fun secondTapCancelsInsteadOfConfirmingTwice() {
        val viewModel = detail("ciclopaseo")
        val before = viewModel.uiState.value.event!!.attendanceCount

        viewModel.toggleAttendance()
        viewModel.toggleAttendance()

        val state = viewModel.uiState.value
        assertEquals(before, state.event!!.attendanceCount)
        assertFalse(state.isAttending)
    }

    @Test
    fun attendanceIsRememberedWhenTheDetailIsOpenedAgain() {
        detail("ciclopaseo").toggleAttendance()
        val before = repository.findPublicEvent("ciclopaseo")!!.attendanceCount

        // Una nueva pantalla de detalle del mismo evento ya sabe que el usuario confirmó
        val reopened = detail("ciclopaseo")
        assertTrue(reopened.uiState.value.isAttending)

        reopened.toggleAttendance()
        assertEquals(before - 1, repository.findPublicEvent("ciclopaseo")!!.attendanceCount)
    }

    @Test
    fun fullEventShowsErrorAndKeepsAttendance() {
        val viewModel = detail("encuentro-vecinal")
        val event = viewModel.uiState.value.event!!
        assertEquals(event.capacity, event.attendanceCount)

        viewModel.toggleAttendance()

        val state = viewModel.uiState.value
        assertEquals(event.attendanceCount, state.event!!.attendanceCount)
        assertFalse(state.isAttending)
        assertEquals(EventDetailViewModel.FULL_EVENT_MESSAGE, state.userMessage)
    }

    @Test
    fun interestTogglesTheCounter() {
        val viewModel = detail("feria-cultural")
        val before = viewModel.uiState.value.event!!.interestCount

        viewModel.toggleInterest()
        assertEquals(before + 1, viewModel.uiState.value.event!!.interestCount)
        assertTrue(viewModel.uiState.value.isInterested)

        viewModel.toggleInterest()
        assertEquals(before, viewModel.uiState.value.event!!.interestCount)
    }

    @Test
    fun changesInTheDetailAreVisibleInTheHome() {
        val home = HomeViewModel(repository)
        val viewModel = detail("ciclopaseo")
        fun homeEvent() = home.uiState.value.let { it.featuredEvents + it.upcomingEvents }.first { it.id == "ciclopaseo" }
        val attendanceBefore = homeEvent().attendanceCount
        val interestBefore = homeEvent().interestCount

        viewModel.toggleAttendance()
        viewModel.toggleInterest()

        assertEquals(attendanceBefore + 1, homeEvent().attendanceCount)
        assertEquals(interestBefore + 1, homeEvent().interestCount)
        assertTrue("ciclopaseo" in home.uiState.value.interestedEventIds)
    }
}
