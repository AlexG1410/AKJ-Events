package co.edu.uniquindio.akjevents.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.normalizedForSearch
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.FinishEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.moderation.RejectEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.moderation.VerifyEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ModerationTab(val label: String, val status: EventStatus) {
    PENDING("Pendientes", EventStatus.PENDING),
    VERIFIED("Verificados", EventStatus.VERIFIED),
    REJECTED("Rechazados", EventStatus.REJECTED),
    FINISHED("Finalizados", EventStatus.FINISHED)
}

/** Causales de rechazo del mockup 17; todas exigen un motivo escrito. */
enum class RejectionReason(val label: String) {
    PLACE_NOT_AUTHORIZED("Lugar no disponible o no autorizado"),
    MISSING_INFORMATION("Información insuficiente"),
    INAPPROPRIATE("Contenido duplicado o inadecuado"),
    OTHER("Otro motivo")
}

data class ModerationItem(val event: CommunityEvent, val organizer: User?, val organizerVerifiedCount: Int)

data class ModerationUiState(
    val moderator: User? = null,
    val query: String = "",
    val tab: ModerationTab = ModerationTab.PENDING,
    val allEvents: List<CommunityEvent> = emptyList(),
    val items: List<ModerationItem> = emptyList(),
    /** Evento abierto en detalle (el primero de la cola, por defecto). */
    val expandedEventId: String? = null,
    val rejectingEventId: String? = null,
    val rejectionReason: RejectionReason? = null,
    val rejectionNote: String = "",
    val userMessage: String? = null
) {
    fun count(tab: ModerationTab): Int = allEvents.count { it.status == tab.status }

    val canConfirmRejection: Boolean
        get() = rejectionReason != null && rejectionNote.isNotBlank()
}

/** Mockup 17: cola de moderación para verificar, rechazar (con motivo) o finalizar eventos. */
@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val verifyEvent: VerifyEventUseCase,
    private val rejectEvent: RejectEventUseCase,
    private val finishEvent: FinishEventUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModerationUiState())
    val uiState: StateFlow<ModerationUiState> = _uiState.asStateFlow()

    // Organizador de cada evento, para mostrarlo y buscar por su nombre sin consultarlo en cada tecla
    private var organizers: Map<String, User?> = emptyMap()

    // Evita que un doble toque apruebe o rechace dos veces mientras la operación termina
    private var isBusy = false

    init {
        viewModelScope.launch {
            combine(observeCurrentUser(), observeEvents(), ::Pair).collect { (moderator, events) ->
                organizers = events.map { it.organizerId }.distinct().associateWith { id ->
                    organizers[id] ?: runCatching { getUserById(id) }.getOrNull()
                }
                _uiState.update { it.copy(moderator = moderator, allEvents = events).filtered() }
            }
        }
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query).filtered() }

    fun selectTab(tab: ModerationTab) = _uiState.update { it.copy(tab = tab, expandedEventId = null, rejectingEventId = null).filtered() }

    fun toggleExpanded(eventId: String) = _uiState.update {
        it.copy(expandedEventId = if (it.expandedEventId == eventId) null else eventId)
    }

    fun verify(eventId: String) {
        val moderator = moderatorId() ?: return
        runAction("No se pudo verificar el evento") {
            if (verifyEvent(eventId, moderator)) {
                _uiState.update { it.copy(expandedEventId = null, userMessage = "Evento verificado y publicado. Se notificó al organizador").filtered() }
            } else {
                showMessage("No se pudo verificar el evento")
            }
        }
    }

    fun startRejection(eventId: String) = _uiState.update {
        it.copy(rejectingEventId = eventId, expandedEventId = eventId, rejectionReason = null, rejectionNote = "")
    }

    fun cancelRejection() = _uiState.update { it.copy(rejectingEventId = null) }

    fun selectRejectionReason(reason: RejectionReason) = _uiState.update { it.copy(rejectionReason = reason) }

    fun onRejectionNoteChange(note: String) = _uiState.update { it.copy(rejectionNote = note.take(MAX_NOTE_LENGTH)) }

    fun confirmRejection() {
        val state = _uiState.value
        val eventId = state.rejectingEventId ?: return
        val moderator = moderatorId() ?: return
        val reason = state.rejectionReason
        if (reason == null || !state.canConfirmRejection) {
            showMessage("Elige una causal y escribe el motivo del rechazo")
            return
        }
        val note = state.rejectionNote.trim()
        val text = when {
            reason == RejectionReason.OTHER -> note
            note.isEmpty() -> reason.label
            else -> "${reason.label}: $note"
        }
        runAction("No se pudo rechazar el evento") {
            if (rejectEvent(eventId, moderator, text)) {
                _uiState.update { it.copy(rejectingEventId = null, expandedEventId = null, userMessage = "Evento rechazado. El organizador recibió el motivo").filtered() }
            } else {
                showMessage("No se pudo rechazar el evento")
            }
        }
    }

    fun finish(eventId: String) {
        val moderator = moderatorId() ?: return
        runAction("No se pudo finalizar el evento") {
            showMessage(if (finishEvent(eventId, moderator)) "Evento marcado como finalizado" else "No se pudo finalizar el evento")
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun moderatorId(): String? = observeCurrentUser().value?.takeIf { it.isModerator }?.id

    private fun showMessage(message: String) = _uiState.update { it.copy(userMessage = message) }

    /** Ejecuta una decisión de moderación; si falla (por ejemplo, sin conexión), muestra [errorMessage]. */
    private fun runAction(errorMessage: String, action: suspend () -> Unit) {
        if (isBusy) return
        isBusy = true
        viewModelScope.launch {
            runCatching { action() }.onFailure { showMessage(errorMessage) }
            isBusy = false
        }
    }

    private fun ModerationUiState.filtered(): ModerationUiState {
        val search = query.normalizedForSearch()
        val items = allEvents
            .filter { it.status == tab.status }
            .mapNotNull { event ->
                val organizer = organizers[event.organizerId]
                val matches = search.isEmpty() ||
                    event.title.normalizedForSearch().contains(search) ||
                    event.address.normalizedForSearch().contains(search) ||
                    organizer?.name?.normalizedForSearch()?.contains(search) == true
                if (!matches) return@mapNotNull null
                val verifiedCount = allEvents.count {
                    it.organizerId == event.organizerId && (it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED)
                }
                ModerationItem(event, organizer, verifiedCount)
            }
            // La cola de pendientes se atiende por orden de llegada
            .let { list -> if (tab == ModerationTab.PENDING) list.sortedBy { it.event.createdAt } else list.sortedByDescending { it.event.startsAt } }
        val expanded = expandedEventId?.takeIf { id -> items.any { it.event.id == id } }
            ?: items.firstOrNull()?.event?.id?.takeIf { tab == ModerationTab.PENDING }
        return copy(items = items, expandedEventId = expanded)
    }

    private companion object {
        const val MAX_NOTE_LENGTH = 300
    }
}
