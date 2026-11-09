package co.edu.uniquindio.akjevents.features.profile.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveCommentsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.ObservePointsHistoryUseCase
import co.edu.uniquindio.akjevents.features.profile.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val pointsHistory: List<PointsEntry> = emptyList(),
    /** Eventos finalizados del usuario, para la galería "Tus huellas en el barrio". */
    val finishedEvents: List<CommunityEvent> = emptyList()
)

/** Mockup 15: dashboard de métricas personales y reputación. */
@HiltViewModel
class StatsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observePointsHistory: ObservePointsHistoryUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeComments: ObserveCommentsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState(user = observeCurrentUser().value))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                combine(observeCurrentUser(), observePointsHistory(), ::Pair),
                observeEvents(),
                observeAttendances(),
                observeComments()
            ) { (user, history), events, attendances, comments ->
                StatsUiState(
                    user = user,
                    stats = user?.let { UserStats.of(it.id, events, attendances, comments) } ?: UserStats(),
                    pointsHistory = history.filter { it.userId == user?.id }.sortedByDescending { it.createdAt },
                    finishedEvents = events.filter { it.organizerId == user?.id && it.status == EventStatus.FINISHED }
                )
            }.collect { state -> _uiState.update { state } }
        }
    }
}
