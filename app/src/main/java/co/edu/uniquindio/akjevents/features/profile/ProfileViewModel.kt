package co.edu.uniquindio.akjevents.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveCommentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val recentEvents: List<CommunityEvent> = emptyList()
)

/** Mockup 12: datos, nivel, puntos, insignias y accesos del usuario de la sesión. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeComments: ObserveCommentsUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(user = observeCurrentUser().value))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeCurrentUser(), observeEvents(), observeAttendances(), observeComments()) { user, events, attendances, comments ->
                ProfileUiState(
                    user = user,
                    stats = user?.let { UserStats.of(it.id, events, attendances, comments) } ?: UserStats(),
                    recentEvents = events.filter { it.organizerId == user?.id }.sortedByDescending { it.createdAt }.take(RECENT_COUNT)
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun logout() {
        viewModelScope.launch { runCatching { logoutUseCase() } }
    }

    private companion object {
        const val RECENT_COUNT = 3
    }
}
