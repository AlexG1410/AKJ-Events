package co.edu.uniquindio.akjevents.features.event.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Comentario con su autor (null si el autor eliminó su cuenta). */
data class CommentItem(val comment: Comment, val author: User?)

data class EventDetailUiState(
    val event: CommunityEvent? = null,
    val viewer: User? = null,
    val organizer: User? = null,
    val organizerEventCount: Int = 0,
    val isInterested: Boolean = false,
    val isAttending: Boolean = false,
    val isCheckedIn: Boolean = false,
    val comments: List<CommentItem> = emptyList(),
    val commentDraft: String = "",
    val userMessage: String? = null
) {
    val isOwner: Boolean get() = event != null && viewer?.id == event.organizerId

    /** Confirmar asistencia y marcar interés solo aplica a eventos públicos y a usuarios (no moderadores). */
    val canParticipate: Boolean get() = event?.status == EventStatus.VERIFIED && viewer?.isModerator == false

    val canComment: Boolean get() = viewer != null && (event?.status == EventStatus.VERIFIED || event?.status == EventStatus.FINISHED)

    val canEdit: Boolean get() = isOwner && event?.status != EventStatus.FINISHED

    val distanceKm: Double? get() = event?.let { viewer?.location?.distanceKmTo(it.location) }
}

/**
 * Detalle de un evento. Recibe el id que llega en la ruta [co.edu.uniquindio.akjevents.navigation.MainRoutes.EventDetail].
 * Los eventos verificados y finalizados los ve cualquiera; los pendientes y rechazados solo su organizador y los moderadores.
 * La asistencia, el interés y los comentarios se guardan en el [EventRepository], así que las demás pantallas ven los cambios.
 */
class EventDetailViewModel(
    private val eventId: String,
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EventDetailUiState().withRepositoryData(
            users.currentUser.value,
            repository.events.value,
            repository.attendances.value,
            repository.interests.value,
            repository.comments.value
        )
    )
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                users.currentUser,
                repository.events,
                repository.attendances,
                repository.interests,
                repository.comments
            ) { viewer, events, attendances, interests, comments ->
                { state: EventDetailUiState -> state.withRepositoryData(viewer, events, attendances, interests, comments) }
            }.collect { transform -> _uiState.update(transform) }
        }
    }

    fun toggleInterest() {
        val viewer = users.currentUser.value ?: return
        val interested = repository.toggleInterest(eventId, viewer.id) ?: return
        showMessage(if (interested) "Guardado en tus eventos de interés" else "Quitado de tus eventos de interés")
    }

    /** Confirma la asistencia o, si ya estaba confirmada, la cancela. */
    fun toggleAttendance() {
        val viewer = users.currentUser.value ?: return
        val state = _uiState.value
        if (state.isAttending) {
            when {
                state.isCheckedIn -> showMessage("Ya registraste tu asistencia con QR; no se puede cancelar")
                repository.cancelAttendance(eventId, viewer.id) -> showMessage("Cancelaste tu asistencia")
            }
            return
        }
        when (repository.confirmAttendance(eventId, viewer.id)) {
            AttendanceResult.CONFIRMED -> showMessage("¡Asistencia confirmada! Te esperamos")
            AttendanceResult.ALREADY_CONFIRMED -> showMessage("Ya confirmaste tu asistencia a este evento")
            AttendanceResult.FULL -> showMessage(FULL_EVENT_MESSAGE)
            AttendanceResult.CLOSED -> showMessage("El evento ya finalizó")
            AttendanceResult.NOT_FOUND -> showMessage("Este evento ya no está disponible")
        }
    }

    fun onCommentChange(text: String) = _uiState.update { it.copy(commentDraft = text.take(MAX_COMMENT_LENGTH)) }

    fun sendComment() {
        val viewer = users.currentUser.value ?: return
        val text = _uiState.value.commentDraft
        if (text.isBlank()) {
            showMessage("Escribe tu comentario antes de enviarlo")
            return
        }
        if (repository.addComment(eventId, viewer.id, text) != null) {
            _uiState.update { it.copy(commentDraft = "", userMessage = "Comentario publicado") }
        } else {
            showMessage("No se pudo publicar el comentario")
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun showMessage(message: String) = _uiState.update { it.copy(userMessage = message) }

    private fun EventDetailUiState.withRepositoryData(
        viewer: User?,
        events: List<CommunityEvent>,
        attendances: List<Attendance>,
        interests: List<Interest>,
        comments: List<Comment>
    ): EventDetailUiState {
        val found = events.find { it.id == eventId }
        val visible = found?.takeIf {
            it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED ||
                viewer?.id == it.organizerId || viewer?.isModerator == true
        }
        val attendance = attendances.find { it.eventId == eventId && it.userId == viewer?.id }
        return copy(
            event = visible,
            viewer = viewer,
            organizer = visible?.let { users.findById(it.organizerId) },
            organizerEventCount = visible?.let { event ->
                events.count { it.organizerId == event.organizerId && (it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED) }
            } ?: 0,
            isAttending = attendance != null,
            isCheckedIn = attendance?.checkedIn == true,
            isInterested = interests.any { it.eventId == eventId && it.userId == viewer?.id },
            comments = if (visible == null) emptyList() else comments
                .filter { it.eventId == eventId }
                .sortedBy { it.createdAt }
                .map { CommentItem(it, users.findById(it.authorId)) }
        )
    }

    companion object {
        const val FULL_EVENT_MESSAGE = "No hay cupos disponibles: el evento está lleno"
        const val MAX_COMMENT_LENGTH = 500
    }
}
