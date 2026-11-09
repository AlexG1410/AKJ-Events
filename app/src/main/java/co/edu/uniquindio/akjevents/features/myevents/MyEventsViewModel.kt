package co.edu.uniquindio.akjevents.features.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.DeleteEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.FinishEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.SendNoticeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestañas del mockup 11; [status] null muestra todos. */
enum class MyEventsTab(val label: String, val status: EventStatus?) {
    ALL("Todos", null),
    PENDING("Pendientes", EventStatus.PENDING),
    VERIFIED("Verificados", EventStatus.VERIFIED),
    REJECTED("Rechazados", EventStatus.REJECTED),
    FINISHED("Finalizados", EventStatus.FINISHED)
}

/** Persona inscrita en un evento; [user] es null si eliminó su cuenta. */
data class AttendeeItem(val user: User?, val checkedIn: Boolean)

data class MyEventsUiState(
    val user: User? = null,
    val tab: MyEventsTab = MyEventsTab.ALL,
    val allEvents: List<CommunityEvent> = emptyList(),
    val attendees: Map<String, List<AttendeeItem>> = emptyMap(),
    val userMessage: String? = null
) {
    val events: List<CommunityEvent>
        get() = allEvents.filter { tab.status == null || it.status == tab.status }

    fun count(tab: MyEventsTab): Int = allEvents.count { tab.status == null || it.status == tab.status }

    /** Porcentaje de eventos aprobados entre los ya revisados por moderación. */
    val approvalRate: Int?
        get() {
            val reviewed = allEvents.filter { it.status != EventStatus.PENDING }
            if (reviewed.isEmpty()) return null
            return (reviewed.count { it.status != EventStatus.REJECTED } * 100f / reviewed.size).roundToInt()
        }
}

/** Mockup 11: los eventos que creó el usuario y lo que puede hacer con cada uno según su estado. */
@HiltViewModel
class MyEventsViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    observeEvents: ObserveEventsUseCase,
    observeAttendances: ObserveAttendancesUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val finishEventUseCase: FinishEventUseCase,
    private val sendNoticeUseCase: SendNoticeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyEventsUiState())
    val uiState: StateFlow<MyEventsUiState> = _uiState.asStateFlow()

    // Evita que un doble toque elimine, finalice o avise dos veces mientras la operación termina
    private var isBusy = false

    init {
        viewModelScope.launch {
            combine(observeCurrentUser(), observeEvents(), observeAttendances(), ::Triple).collect { (user, events, attendances) ->
                val own = events.filter { it.organizerId == user?.id }.sortedByDescending { it.createdAt }
                val attendees = attendeesOf(own, attendances)
                _uiState.update { it.copy(user = user, allEvents = own, attendees = attendees) }
            }
        }
    }

    fun selectTab(tab: MyEventsTab) = _uiState.update { it.copy(tab = tab) }

    fun deleteEvent(eventId: String) {
        val user = observeCurrentUser().value ?: return
        runAction("No se pudo eliminar el evento") {
            showMessage(if (deleteEventUseCase(eventId, user.id)) "Evento eliminado" else "No se pudo eliminar el evento")
        }
    }

    fun finishEvent(eventId: String) {
        val user = observeCurrentUser().value ?: return
        runAction("No se pudo finalizar el evento") {
            showMessage(if (finishEventUseCase(eventId, user.id)) "Evento marcado como finalizado" else "No se pudo finalizar el evento")
        }
    }

    fun sendNotice(eventId: String, message: String) {
        val user = observeCurrentUser().value ?: return
        if (message.isBlank()) {
            showMessage("Escribe el aviso antes de enviarlo")
            return
        }
        runAction("No se pudo enviar el aviso") {
            val sent = sendNoticeUseCase(eventId, user.id, message)
            showMessage(
                when (sent) {
                    0 -> "Aún no hay personas inscritas con cuenta para avisarles"
                    1 -> "Aviso enviado a 1 persona"
                    else -> "Aviso enviado a $sent personas"
                }
            )
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun showMessage(message: String) = _uiState.update { it.copy(userMessage = message) }

    /** Ejecuta una acción sobre un evento; si falla (por ejemplo, sin conexión), muestra [errorMessage]. */
    private fun runAction(errorMessage: String, action: suspend () -> Unit) {
        if (isBusy) return
        isBusy = true
        viewModelScope.launch {
            runCatching { action() }.onFailure { showMessage(errorMessage) }
            isBusy = false
        }
    }

    /** Inscritos de cada evento propio, con su cuenta (null si la eliminó o no se pudo cargar). */
    private suspend fun attendeesOf(own: List<CommunityEvent>, attendances: List<Attendance>): Map<String, List<AttendeeItem>> {
        val ownIds = own.map { it.id }.toSet()
        val mine = attendances.filter { it.eventId in ownIds }
        val users: Map<String, User?> = mine.map { it.userId }.distinct().associateWith { id ->
            runCatching { getUserById(id) }.getOrNull()
        }
        return mine.groupBy { it.eventId }.mapValues { (_, list) -> list.map { AttendeeItem(users[it.userId], it.checkedIn) } }
    }
}
