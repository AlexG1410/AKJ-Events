package co.edu.uniquindio.akjevents.features.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.CheckInResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckInTab { SCAN, MY_QR }

data class CheckInUiState(
    val user: User? = null,
    val tab: CheckInTab = CheckInTab.SCAN,
    val code: String = "",
    /** Eventos con asistencia confirmada que aún esperan el check-in. */
    val pendingEvents: List<CommunityEvent> = emptyList(),
    val result: CheckInResult? = null,
    /** Puntos y nivel antes del check-in, para mostrar el avance. */
    val pointsBefore: Int = 0,
    val organizer: User? = null,
    val errorMessage: String? = null
)

/**
 * Mockup 16. En esta fase no se usa la cámara (no se piden permisos): el escaneo se simula
 * escribiendo el código que muestra el QR del organizador o eligiendo un evento confirmado.
 */
class CheckInViewModel(
    preselectedEventId: String? = null,
    showMyQr: Boolean = false,
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        CheckInUiState(
            user = users.currentUser.value,
            tab = if (showMyQr) CheckInTab.MY_QR else CheckInTab.SCAN,
            code = preselectedEventId?.let { repository.findPublicEvent(it)?.checkInCode }.orEmpty()
        )
    )
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(users.currentUser, repository.publicEvents, repository.attendances, ::Triple).collect { (user, events, attendances) ->
                val pendingIds = attendances.filter { it.userId == user?.id && !it.checkedIn }.map { it.eventId }.toSet()
                _uiState.update { state ->
                    state.copy(user = user, pendingEvents = events.filter { it.id in pendingIds }.sortedBy { it.startsAt })
                }
            }
        }
    }

    fun selectTab(tab: CheckInTab) = _uiState.update { it.copy(tab = tab) }

    fun onCodeChange(code: String) = _uiState.update { it.copy(code = code.uppercase().take(MAX_CODE_LENGTH), errorMessage = null) }

    /** Simula leer el QR del organizador de ese evento. */
    fun scanEvent(event: CommunityEvent) {
        onCodeChange(event.checkInCode)
        validate()
    }

    fun validate() {
        val user = users.currentUser.value ?: return
        val code = _uiState.value.code
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Escribe o escanea el código del evento") }
            return
        }
        val pointsBefore = user.points
        when (val result = repository.checkIn(code, user.id)) {
            is CheckInResult.Success -> _uiState.update {
                it.copy(result = result, pointsBefore = pointsBefore, organizer = users.findById(result.event.organizerId), errorMessage = null)
            }
            is CheckInResult.AlreadyCheckedIn -> showError("Ya registraste tu asistencia en «${result.event.title}»")
            is CheckInResult.Full -> showError("«${result.event.title}» ya no tiene cupos disponibles")
            is CheckInResult.OwnEvent -> showError("No puedes hacer check-in en tu propio evento")
            CheckInResult.InvalidCode -> showError("Código no válido o el evento no está activo")
        }
    }

    /** Vuelve a la cámara simulada para otro check-in. */
    fun reset() = _uiState.update { it.copy(result = null, code = "", errorMessage = null) }

    private fun showError(message: String) = _uiState.update { it.copy(errorMessage = message, result = null) }

    private companion object {
        const val MAX_CODE_LENGTH = 12
    }
}
