package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.TestRepositories.Companion.MODERATOR
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.features.moderation.ModerationTab
import co.edu.uniquindio.akjevents.features.moderation.ModerationViewModel
import co.edu.uniquindio.akjevents.features.moderation.RejectionReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ModerationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories(sessionUserId = MODERATOR)
    private val viewModel by lazy { ModerationViewModel(repos.events, repos.users) }

    private fun ids() = viewModel.uiState.value.items.map { it.event.id }

    @Test
    fun pendingQueueIsOrderedByArrivalAndTheFirstIsExpanded() {
        val state = viewModel.uiState.value

        assertEquals(4, state.count(ModerationTab.PENDING))
        assertEquals(listOf("pendiente-privado", "muestra-artesanal", "huertos-urbanos", "microfutbol"), ids())
        assertEquals("pendiente-privado", state.expandedEventId)
    }

    @Test
    fun verifyingMovesTheEventToVerified() {
        viewModel.verify("huertos-urbanos")

        assertFalse("huertos-urbanos" in ids())
        assertEquals(EventStatus.VERIFIED, repos.events.findEvent("huertos-urbanos")!!.status)
        assertNotNull(viewModel.uiState.value.userMessage)
        // La siguiente ficha de la cola queda abierta para seguir revisando
        assertEquals("pendiente-privado", viewModel.uiState.value.expandedEventId)
    }

    @Test
    fun rejectingRequiresAReasonAndSendsIt() {
        viewModel.startRejection("huertos-urbanos")
        viewModel.confirmRejection()
        assertEquals(EventStatus.PENDING, repos.events.findEvent("huertos-urbanos")!!.status)

        // Todas las causales exigen escribir un motivo, no solo "Otro motivo".
        viewModel.selectRejectionReason(RejectionReason.OTHER)
        assertFalse(viewModel.uiState.value.canConfirmRejection)

        viewModel.selectRejectionReason(RejectionReason.PLACE_NOT_AUTHORIZED)
        viewModel.onRejectionNoteChange("Solicita el permiso del parque")
        viewModel.confirmRejection()

        val event = repos.events.findEvent("huertos-urbanos")!!
        assertEquals(EventStatus.REJECTED, event.status)
        assertEquals("Lugar no disponible o no autorizado: Solicita el permiso del parque", event.rejectionReason)
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.EVENT_REJECTED })
    }

    @Test
    fun searchFiltersByOrganizerName() {
        viewModel.onQueryChange("camilo")

        assertEquals(listOf("huertos-urbanos"), ids())
    }

    @Test
    fun moderatorCanFinishAVerifiedEvent() {
        viewModel.selectTab(ModerationTab.VERIFIED)
        assertTrue("ciclopaseo" in ids())

        viewModel.finish("ciclopaseo")

        assertFalse("ciclopaseo" in ids())
        assertEquals(EventStatus.FINISHED, repos.events.findEvent("ciclopaseo")!!.status)
    }

    @Test
    fun aRegularUserCannotModerate() {
        val asUser = TestRepositories()
        val userViewModel = ModerationViewModel(asUser.events, asUser.users)

        userViewModel.verify("huertos-urbanos")

        assertEquals(EventStatus.PENDING, asUser.events.findEvent("huertos-urbanos")!!.status)
    }

    @Test
    fun everyRejectionCategoryRequiresANonBlankWrittenReason() {
        RejectionReason.entries.forEach { reason ->
            viewModel.startRejection("huertos-urbanos")
            viewModel.selectRejectionReason(reason)
            listOf("", "   \n").forEach { note ->
                viewModel.onRejectionNoteChange(note)
                assertFalse(viewModel.uiState.value.canConfirmRejection)
                viewModel.confirmRejection()
                assertEquals(EventStatus.PENDING, repos.events.findEvent("huertos-urbanos")!!.status)
            }
            viewModel.onRejectionNoteChange("Describe mejor el lugar del evento")
            assertTrue(viewModel.uiState.value.canConfirmRejection)
        }
        assertTrue(repos.notificationsOf(DEMO_USER).none { it.type == NotificationType.EVENT_REJECTED })
    }
}
