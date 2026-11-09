package co.edu.uniquindio.akjevents

import kotlinx.coroutines.test.runTest
import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.domain.model.NotificationGroup
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.features.notifications.NotificationsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories()
    private val viewModel by lazy { repos.useCases.let {
        NotificationsViewModel(it.observeCurrentUser, it.observeNotifications, it.setNotificationRead, it.markAllNotificationsRead)
    } }

    private suspend fun notify(type: NotificationType, userId: String = DEMO_USER) =
        repos.notifications.notify(userId, type, "Título", "Mensaje", "ciclopaseo")

    @Test
    fun showsOnlyTheSessionUsersNotifications() = runTest {
        notify(NotificationType.NEW_COMMENT)
        notify(NotificationType.NEW_COMMENT, userId = "organizer-1")

        assertEquals(1, viewModel.uiState.value.all.size)
    }

    @Test
    fun notificationsCanBeMarkedReadAndUnread() = runTest {
        notify(NotificationType.EVENT_VERIFIED)
        val notification = viewModel.uiState.value.all.single()
        assertEquals(1, viewModel.uiState.value.unreadCount)

        viewModel.toggleRead(notification)
        assertTrue(viewModel.uiState.value.all.single().isRead)

        viewModel.toggleRead(viewModel.uiState.value.all.single())
        assertFalse(viewModel.uiState.value.all.single().isRead)

        viewModel.markAllAsRead()
        assertEquals(0, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun filterByGroup() = runTest {
        notify(NotificationType.NEW_COMMENT)
        notify(NotificationType.EVENT_REJECTED)
        notify(NotificationType.BADGE_UNLOCKED)

        viewModel.selectFilter(NotificationGroup.MODERATION)
        assertEquals(listOf(NotificationType.EVENT_REJECTED), viewModel.uiState.value.visible.map { it.type })
        assertEquals(3, viewModel.uiState.value.count(null))
    }

    @Test
    fun aCommentOnMyEventArrivesAsANotification() = runTest {
        repos.useCases.addComment("cine-foro", "organizer-5", "¿A qué hora empieza?")

        val notification = viewModel.uiState.value.all.single()
        assertEquals(NotificationType.NEW_COMMENT, notification.type)
        assertEquals("cine-foro", notification.eventId)
    }
}
