package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryEventRepositoryTest {
    private val repository = InMemoryEventRepository()

    private fun attendance(id: String) = repository.findPublicEvent(id)!!.attendanceCount

    @Test
    fun onlyVerifiedEventsArePublic() {
        val events = repository.publicEvents.value

        assertEquals(5, events.size)
        assertTrue(events.all { it.status == EventStatus.VERIFIED })
        assertNull(repository.findPublicEvent("pendiente-privado"))
        assertNull(repository.findPublicEvent("rechazado-privado"))
    }

    @Test
    fun hiddenEventsCannotBeModified() {
        assertEquals(AttendanceResult.NOT_FOUND, repository.confirmAttendance("pendiente-privado"))
        assertNull(repository.toggleInterest("rechazado-privado"))
        assertTrue(repository.attendingEventIds.value.isEmpty())
        assertTrue(repository.interestedEventIds.value.isEmpty())
    }

    @Test
    fun confirmAddsOneAttendeeOnlyOnce() {
        val before = attendance("ciclopaseo")

        assertEquals(AttendanceResult.CONFIRMED, repository.confirmAttendance("ciclopaseo"))
        assertEquals(AttendanceResult.ALREADY_CONFIRMED, repository.confirmAttendance("ciclopaseo"))

        assertEquals(before + 1, attendance("ciclopaseo"))
        assertEquals(setOf("ciclopaseo"), repository.attendingEventIds.value)
    }

    @Test
    fun cancelRemovesTheAttendee() {
        val before = attendance("ciclopaseo")
        repository.confirmAttendance("ciclopaseo")

        assertTrue(repository.cancelAttendance("ciclopaseo"))
        assertFalse(repository.cancelAttendance("ciclopaseo"))

        assertEquals(before, attendance("ciclopaseo"))
        assertTrue(repository.attendingEventIds.value.isEmpty())
    }

    @Test
    fun fullEventRejectsConfirmation() {
        val before = attendance("encuentro-vecinal")

        assertEquals(AttendanceResult.FULL, repository.confirmAttendance("encuentro-vecinal"))

        assertEquals(before, attendance("encuentro-vecinal"))
        assertFalse("encuentro-vecinal" in repository.attendingEventIds.value)
    }

    @Test
    fun interestTogglesCounterAndSet() {
        val before = repository.findPublicEvent("feria-cultural")!!.interestCount

        assertEquals(true, repository.toggleInterest("feria-cultural"))
        assertEquals(before + 1, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertTrue("feria-cultural" in repository.interestedEventIds.value)

        assertEquals(false, repository.toggleInterest("feria-cultural"))
        assertEquals(before, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertFalse("feria-cultural" in repository.interestedEventIds.value)
    }
}
