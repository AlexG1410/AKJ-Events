package co.edu.uniquindio.akjevents.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.RestoreSessionUseCase
import co.edu.uniquindio.akjevents.domain.usecase.notification.ObserveNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SessionUiState(
    val user: User? = null,
    val unreadAlerts: Int = 0,
    /** True cuando ya se sabe si hay sesión abierta; antes, el splash no decide a dónde ir. */
    val isReady: Boolean = false
) {
    val isModerator: Boolean get() = user?.isModerator == true
}

/** Datos de la sesión que necesita la navegación: el rol (barra inferior) y las alertas sin leer. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeNotifications: ObserveNotificationsUseCase,
    restoreSession: RestoreSessionUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    // Se marca al terminar de revisar la sesión guardada (y cerrarla si no se pidió recordar el dispositivo)
    private val restored = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            runCatching { restoreSession() }
            restored.value = true
        }
        viewModelScope.launch {
            combine(observeCurrentUser(), observeNotifications(), restored) { user, all, isRestored ->
                SessionUiState(
                    user = user,
                    unreadAlerts = all.count { it.userId == user?.id && !it.isRead },
                    isReady = isRestored
                )
            }.collect { _uiState.value = it }
        }
    }

    fun logout() {
        viewModelScope.launch { runCatching { logoutUseCase() } }
    }
}
