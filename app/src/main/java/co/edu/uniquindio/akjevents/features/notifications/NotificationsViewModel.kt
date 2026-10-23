package co.edu.uniquindio.akjevents.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryNotificationRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.AppNotification
import co.edu.uniquindio.akjevents.domain.model.NotificationGroup
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val user: User? = null,
    /** null muestra todas. */
    val filter: NotificationGroup? = null,
    val all: List<AppNotification> = emptyList()
) {
    val visible: List<AppNotification> get() = all.filter { filter == null || it.type.group == filter }
    val unreadCount: Int get() = all.count { !it.isRead }

    fun count(group: NotificationGroup?): Int = all.count { group == null || it.type.group == group }
}

/** Mockup 14: notificaciones del usuario de la sesión, que se pueden marcar como leídas o no leídas. */
class NotificationsViewModel(
    private val notifications: NotificationRepository = InMemoryNotificationRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(users.currentUser, notifications.notifications) { user, all ->
                user to all.filter { it.userId == user?.id }.sortedByDescending { it.createdAt }
            }.collect { (user, mine) -> _uiState.update { it.copy(user = user, all = mine) } }
        }
    }

    fun selectFilter(group: NotificationGroup?) = _uiState.update { it.copy(filter = group) }

    fun toggleRead(notification: AppNotification) = notifications.setRead(notification.id, !notification.isRead)

    /** Al abrir una notificación se marca como leída. */
    fun markAsRead(notification: AppNotification) = notifications.setRead(notification.id, true)

    fun markAllAsRead() {
        val user = users.currentUser.value ?: return
        notifications.markAllAsRead(user.id)
    }
}
