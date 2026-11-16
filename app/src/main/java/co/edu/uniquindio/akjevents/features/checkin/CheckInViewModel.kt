package co.edu.uniquindio.akjevents.features.checkin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.GetPublicEventByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObservePublicEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInResult
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.GetUserByIdUseCase
import co.edu.uniquindio.akjevents.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

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
    val errorMessage: String? = null,
    /** El código se está validando; evita enviarlo dos veces. */
    val isValidating: Boolean = false,
    val expectedEventId: String? = null
)

/**
 * Cámara real desde la UI; entrada manual alternativa. Elegir un evento no simula su lectura.
 */
@HiltViewModel
class CheckInViewModel(
    preselectedEventId: String?,
    showMyQr: Boolean,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observePublicEvents: ObservePublicEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    private val getPublicEventById: GetPublicEventByIdUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val checkIn: CheckInUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    /** Hilt lee el evento preseleccionado de la ruta [MainRoutes.CheckIn]. */
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeCurrentUser: ObserveCurrentUserUseCase,
        observePublicEvents: ObservePublicEventsUseCase,
        observeAttendances: ObserveAttendancesUseCase,
        getPublicEventById: GetPublicEventByIdUseCase,
        getUserById: GetUserByIdUseCase,
        checkIn: CheckInUseCase,
        resources: ResourceProvider
    ) : this(
        savedStateHandle.toRoute<MainRoutes.CheckIn>().eventId,
        savedStateHandle.toRoute<MainRoutes.CheckIn>().showMyQr,
        observeCurrentUser, observePublicEvents, observeAttendances, getPublicEventById, getUserById, checkIn, resources
    )

    private val _uiState = MutableStateFlow(
        CheckInUiState(
            user = observeCurrentUser().value,
            tab = if (showMyQr) CheckInTab.MY_QR else CheckInTab.SCAN,
            expectedEventId = preselectedEventId
        )
    )
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeCurrentUser(), observePublicEvents(), observeAttendances(), ::Triple).collect { (user, events, attendances) ->
                val pendingIds = attendances.filter { it.userId == user?.id && !it.checkedIn }.map { it.eventId }.toSet()
                _uiState.update { state ->
                    state.copy(user = user, pendingEvents = events.filter { it.id in pendingIds && it.organizerId != user?.id }.sortedBy { it.startsAt })
                }
            }
        }
    }

    fun selectTab(tab: CheckInTab) = _uiState.update { it.copy(tab = tab) }

    fun onCodeChange(code: String) = _uiState.update { it.copy(code = code.uppercase().take(MAX_CODE_LENGTH), errorMessage = null) }

    /** Selecciona qué evento se espera; la UI debe abrir la cámara, no rellenar su código. */
    fun selectEventForScan(event: CommunityEvent) {
        _uiState.update { it.copy(expectedEventId = event.id, code = "", errorMessage = null) }
    }

    fun onScannedCode(code: String?) {
        if (_uiState.value.isValidating || _uiState.value.result is CheckInResult.Success) return
        if (code == null) { showError(resources.getString(R.string.qr_cancelled)); return }
        val normalized = code.trim().uppercase(java.util.Locale.ROOT)
        if (!Regex("AKJ-[A-Z0-9]{6}").matches(normalized)) {
            showError(resources.getString(R.string.check_in_error_invalid)); return
        }
        onCodeChange(normalized)
        validate()
    }

    fun onScannerMessage(message: String) = showError(message)

    fun validate() {
        val user = observeCurrentUser().value ?: return
        val state = _uiState.value
        if (state.isValidating) return
        val code = state.code
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = resources.getString(R.string.check_in_error_empty)) }
            return
        }
        val pointsBefore = user.points
        _uiState.update { it.copy(isValidating = true, errorMessage = null) }
        viewModelScope.launch {
            val result = runCatching { checkIn(code, user.id, state.expectedEventId) }.getOrElse {
                if (it is CancellationException) throw it
                showError(resources.getString(R.string.check_in_error_generic))
                return@launch
            }
            if (observeCurrentUser().value?.id != user.id) {
                _uiState.update { it.copy(isValidating = false, result = null, code = "") }
                return@launch
            }
            when (result) {
                is CheckInResult.Success -> {
                    val organizer = runCatching { getUserById(result.event.organizerId) }.getOrElse {
                        if (it is CancellationException) throw it
                        null
                    }
                    _uiState.update {
                        it.copy(result = result, pointsBefore = result.pointsBefore ?: pointsBefore, organizer = organizer, errorMessage = null, isValidating = false)
                    }
                }
                is CheckInResult.AlreadyCheckedIn -> showError(resources.getString(R.string.check_in_error_already, result.event.title))
                is CheckInResult.Full -> showError(resources.getString(R.string.check_in_error_full, result.event.title))
                is CheckInResult.OwnEvent -> showError(resources.getString(R.string.check_in_error_own_event))
                CheckInResult.InvalidCode -> showError(resources.getString(R.string.check_in_error_invalid))
                CheckInResult.WrongEvent -> showError(resources.getString(R.string.qr_wrong_event))
            }
        }
    }

    /** La cámara solo se abre mediante una acción explícita. */
    fun reset() = _uiState.update { it.copy(result = null, code = "", errorMessage = null, expectedEventId = null) }

    private fun showError(message: String) = _uiState.update { it.copy(errorMessage = message, result = null, isValidating = false) }

    private companion object {
        const val MAX_CODE_LENGTH = 12
    }
}
