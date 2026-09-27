package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicEventVisibilityTest {
    @Test
    fun pendingAndRejectedEventsAreNotPubliclyAccessible() {
        assertEquals(5, SampleEvents.all.count { it.status == EventStatus.VERIFIED })
        assertNull(SampleEvents.findPublicById("pendiente-privado"))
        assertNull(SampleEvents.findPublicById("rechazado-privado"))
    }
}
