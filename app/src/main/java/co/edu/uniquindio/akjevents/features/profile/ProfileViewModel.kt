package co.edu.uniquindio.akjevents.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
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
class ProfileViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(user = users.currentUser.value))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(users.currentUser, repository.events, repository.attendances, repository.comments) { user, events, attendances, comments ->
                ProfileUiState(
                    user = user,
                    stats = user?.let { UserStats.of(it.id, events, attendances, comments) } ?: UserStats(),
                    recentEvents = events.filter { it.organizerId == user?.id }.sortedByDescending { it.createdAt }.take(RECENT_COUNT)
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun logout() = users.logout()

    private companion object {
        const val RECENT_COUNT = 3
    }
}
