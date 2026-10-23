package co.edu.uniquindio.akjevents.features.myevents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
class MyEventsViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyEventsUiState())
    val uiState: StateFlow<MyEventsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(users.currentUser, repository.events, repository.attendances, ::Triple).collect { (user, events, attendances) ->
                _uiState.update { it.withData(user, events, attendances) }
            }
        }
    }

    fun selectTab(tab: MyEventsTab) = _uiState.update { it.copy(tab = tab) }

    fun deleteEvent(eventId: String) {
        val user = users.currentUser.value ?: return
        showMessage(if (repository.deleteEvent(eventId, user.id)) "Evento eliminado" else "No se pudo eliminar el evento")
    }

    fun finishEvent(eventId: String) {
        val user = users.currentUser.value ?: return
        showMessage(if (repository.finishEvent(eventId, user.id)) "Evento marcado como finalizado" else "No se pudo finalizar el evento")
    }

    fun sendNotice(eventId: String, message: String) {
        val user = users.currentUser.value ?: return
        if (message.isBlank()) {
            showMessage("Escribe el aviso antes de enviarlo")
            return
        }
        val sent = repository.sendNotice(eventId, user.id, message)
        showMessage(
            when (sent) {
                0 -> "Aún no hay personas inscritas con cuenta para avisarles"
                1 -> "Aviso enviado a 1 persona"
                else -> "Aviso enviado a $sent personas"
            }
        )
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun showMessage(message: String) = _uiState.update { it.copy(userMessage = message) }

    private fun MyEventsUiState.withData(user: User?, events: List<CommunityEvent>, attendances: List<Attendance>): MyEventsUiState {
        val own = events.filter { it.organizerId == user?.id }.sortedByDescending { it.createdAt }
        val ownIds = own.map { it.id }.toSet()
        return copy(
            user = user,
            allEvents = own,
            attendees = attendances.filter { it.eventId in ownIds }
                .groupBy { it.eventId }
                .mapValues { (_, list) -> list.map { AttendeeItem(users.findById(it.userId), it.checkedIn) } }
        )
    }
}
