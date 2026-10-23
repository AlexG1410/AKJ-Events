package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.features.myevents.MyEventsTab
import co.edu.uniquindio.akjevents.features.myevents.MyEventsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MyEventsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories()
    private val viewModel by lazy { MyEventsViewModel(repos.events, repos.users) }

    @Test
    fun listsOnlyTheUsersEventsWithCountsPerStatus() {
        val state = viewModel.uiState.value

        assertEquals(4, state.count(MyEventsTab.ALL))
        assertEquals(1, state.count(MyEventsTab.PENDING))
        assertEquals(1, state.count(MyEventsTab.VERIFIED))
        assertEquals(1, state.count(MyEventsTab.REJECTED))
        assertEquals(1, state.count(MyEventsTab.FINISHED))
        assertTrue(state.allEvents.all { it.organizerId == TestRepositories.DEMO_USER })
    }

    @Test
    fun tabsFilterByStatus() {
        viewModel.selectTab(MyEventsTab.REJECTED)

        assertEquals(listOf("voleibol-callejero"), viewModel.uiState.value.events.map { it.id })
    }

    @Test
    fun attendeesAreListedPerEvent() {
        val attendees = viewModel.uiState.value.attendees["cine-foro"].orEmpty()

        assertEquals(setOf("organizer-4", "organizer-7"), attendees.mapNotNull { it.user?.id }.toSet())
    }

    @Test
    fun finishingAndDeletingUpdateTheList() {
        viewModel.finishEvent("cine-foro")
        assertEquals(EventStatus.FINISHED, repos.events.findEvent("cine-foro")!!.status)
        assertEquals(2, viewModel.uiState.value.count(MyEventsTab.FINISHED))

        viewModel.deleteEvent("huertos-urbanos")
        assertNull(repos.events.findEvent("huertos-urbanos"))
        assertEquals(3, viewModel.uiState.value.count(MyEventsTab.ALL))
    }

    @Test
    fun noticeReachesTheAttendees() {
        viewModel.sendNotice("cine-foro", "La proyección empieza 15 minutos tarde")

        assertEquals("Aviso enviado a 2 personas", viewModel.uiState.value.userMessage)
        assertTrue(repos.notificationsOf("organizer-4").any { it.type == NotificationType.ORGANIZER_NOTICE })
    }
}
