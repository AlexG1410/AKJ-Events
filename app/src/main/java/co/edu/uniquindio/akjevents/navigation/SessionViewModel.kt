package co.edu.uniquindio.akjevents.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryNotificationRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SessionUiState(
    val user: User? = null,
    val unreadAlerts: Int = 0
) {
    val isModerator: Boolean get() = user?.isModerator == true
}

/** Datos de la sesión que necesita la navegación: el rol (barra inferior) y las alertas sin leer. */
class SessionViewModel(
    private val users: UserRepository = InMemoryUserRepository.shared,
    notifications: NotificationRepository = InMemoryNotificationRepository.shared
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(users.currentUser, notifications.notifications) { user, all ->
                SessionUiState(user = user, unreadAlerts = all.count { it.userId == user?.id && !it.isRead })
            }.collect { _uiState.value = it }
        }
    }

    fun logout() = users.logout()
}
