package co.edu.uniquindio.akjevents.features.event.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Interest
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.AddCommentUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.CancelAttendanceUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ConfirmAttendanceUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveCommentsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveInterestsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ToggleInterestUseCase
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
 * La asistencia, el interés y los comentarios pasan por sus casos de uso, así que las demás pantallas ven los cambios.
 */
@HiltViewModel
class EventDetailViewModel(
    private val eventId: String,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    observeInterests: ObserveInterestsUseCase,
    observeComments: ObserveCommentsUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val toggleInterestUseCase: ToggleInterestUseCase,
    private val confirmAttendance: ConfirmAttendanceUseCase,
    private val cancelAttendance: CancelAttendanceUseCase,
    private val addComment: AddCommentUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    /** Hilt lee el id del evento de la ruta [MainRoutes.EventDetail]. */
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeCurrentUser: ObserveCurrentUserUseCase,
        observeEvents: ObserveEventsUseCase,
        observeAttendances: ObserveAttendancesUseCase,
        observeInterests: ObserveInterestsUseCase,
        observeComments: ObserveCommentsUseCase,
        getUserById: GetUserByIdUseCase,
        toggleInterestUseCase: ToggleInterestUseCase,
        confirmAttendance: ConfirmAttendanceUseCase,
        cancelAttendance: CancelAttendanceUseCase,
        addComment: AddCommentUseCase,
        resources: ResourceProvider
    ) : this(
        savedStateHandle.toRoute<MainRoutes.EventDetail>().eventId,
        observeCurrentUser, observeEvents, observeAttendances, observeInterests, observeComments,
        getUserById, toggleInterestUseCase, confirmAttendance, cancelAttendance, addComment, resources
    )

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    // Evita que un doble toque confirme o comente dos veces mientras la operación termina
    private var isBusy = false

    init {
        viewModelScope.launch {
            combine(
                observeCurrentUser(),
                observeEvents(),
                observeAttendances(),
                observeInterests(),
                observeComments()
            ) { viewer, events, attendances, interests, comments ->
                detailData(viewer, events, attendances, interests, comments)
            }.collect { data ->
                _uiState.update {
                    it.copy(
                        event = data.event,
                        viewer = data.viewer,
                        organizer = data.organizer,
                        organizerEventCount = data.organizerEventCount,
                        isAttending = data.isAttending,
                        isCheckedIn = data.isCheckedIn,
                        isInterested = data.isInterested,
                        comments = data.comments
                    )
                }
            }
        }
    }

    fun toggleInterest() {
        val viewer = observeCurrentUser().value ?: return
        runAction(R.string.detail_error_interest) {
            val interested = toggleInterestUseCase(eventId, viewer.id) ?: return@runAction
            showMessage(if (interested) R.string.detail_interest_saved else R.string.detail_interest_removed)
        }
    }

    /** Confirma la asistencia o, si ya estaba confirmada, la cancela. */
    fun toggleAttendance() {
        val viewer = observeCurrentUser().value ?: return
        val state = _uiState.value
        if (state.isAttending && state.isCheckedIn) {
            showMessage(R.string.detail_cannot_cancel)
            return
        }
        runAction(R.string.detail_error_attendance) {
            if (state.isAttending) {
                if (cancelAttendance(eventId, viewer.id)) showMessage(R.string.detail_attendance_cancelled)
                return@runAction
            }
            when (confirmAttendance(eventId, viewer.id)) {
                AttendanceResult.CONFIRMED -> showMessage(R.string.detail_attendance_confirmed)
                AttendanceResult.ALREADY_CONFIRMED -> showMessage(R.string.detail_attendance_already)
                AttendanceResult.FULL -> showMessage(R.string.detail_event_full)
                AttendanceResult.CLOSED -> showMessage(R.string.detail_event_closed)
                AttendanceResult.NOT_FOUND -> showMessage(R.string.detail_event_gone)
            }
        }
    }

    fun onCommentChange(text: String) = _uiState.update { it.copy(commentDraft = text.take(MAX_COMMENT_LENGTH)) }

    fun sendComment() {
        val viewer = observeCurrentUser().value ?: return
        val text = _uiState.value.commentDraft
        if (text.isBlank()) {
            showMessage(R.string.detail_comment_empty)
            return
        }
        runAction(R.string.detail_error_comment) {
            if (addComment(eventId, viewer.id, text) != null) {
                _uiState.update { it.copy(commentDraft = "", userMessage = resources.getString(R.string.detail_comment_posted)) }
            } else {
                showMessage(R.string.detail_error_comment)
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun showMessage(@StringRes message: Int) = _uiState.update { it.copy(userMessage = resources.getString(message)) }

    /** Ejecuta una acción del usuario; si falla (por ejemplo, sin conexión), muestra [errorMessage]. */
    private fun runAction(@StringRes errorMessage: Int, action: suspend () -> Unit) {
        if (isBusy) return
        isBusy = true
        viewModelScope.launch {
            runCatching { action() }.onFailure { showMessage(errorMessage) }
            isBusy = false
        }
    }

    /** Lo que la pantalla muestra a partir de los datos observados. */
    private class DetailData(
        val event: CommunityEvent?,
        val viewer: User?,
        val organizer: User?,
        val organizerEventCount: Int,
        val isAttending: Boolean,
        val isCheckedIn: Boolean,
        val isInterested: Boolean,
        val comments: List<CommentItem>
    )

    private suspend fun detailData(
        viewer: User?,
        events: List<CommunityEvent>,
        attendances: List<Attendance>,
        interests: List<Interest>,
        comments: List<Comment>
    ): DetailData {
        val found = events.find { it.id == eventId }
        val visible = found?.takeIf {
            it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED ||
                viewer?.id == it.organizerId || viewer?.isModerator == true
        }
        val attendance = attendances.find { it.eventId == eventId && it.userId == viewer?.id }
        return DetailData(
            event = visible,
            viewer = viewer,
            organizer = visible?.let { findUser(it.organizerId) },
            organizerEventCount = visible?.let { event ->
                events.count { it.organizerId == event.organizerId && (it.status == EventStatus.VERIFIED || it.status == EventStatus.FINISHED) }
            } ?: 0,
            isAttending = attendance != null,
            isCheckedIn = attendance?.checkedIn == true,
            isInterested = interests.any { it.eventId == eventId && it.userId == viewer?.id },
            comments = if (visible == null) emptyList() else comments
                .filter { it.eventId == eventId }
                .sortedBy { it.createdAt }
                .map { CommentItem(it, findUser(it.authorId)) }
        )
    }

    // Un autor que no se pudo cargar se muestra como cuenta eliminada en lugar de romper la pantalla
    private suspend fun findUser(id: String): User? = runCatching { getUserById(id) }.getOrNull()

    companion object {
        const val MAX_COMMENT_LENGTH = 500
    }
}
