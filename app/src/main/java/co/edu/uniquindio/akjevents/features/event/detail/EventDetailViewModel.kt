package co.edu.uniquindio.akjevents.features.event.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val event: CommunityEvent? = null,
    val organizer: User? = null,
    val organizerEventCount: Int = 0,
    val isInterested: Boolean = false,
    val isAttending: Boolean = false,
    val userMessage: String? = null
)

/**
 * Detalle de un evento público. Recibe el id que llega en la ruta [co.edu.uniquindio.akjevents.navigation.MainRoutes.EventDetail].
 * La asistencia y el interés se guardan en el [EventRepository], así que el Home ve los mismos cambios.
 */
class EventDetailViewModel(
    private val eventId: String,
    private val repository: EventRepository = InMemoryEventRepository.shared,
    findOrganizer: (String) -> User? = SampleUsers::findById
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        // El repositorio solo devuelve eventos públicos: los demás quedan como no disponibles
        repository.findPublicEvent(eventId).let { event ->
            EventDetailUiState(organizer = event?.let { findOrganizer(it.organizerId) })
                .withRepositoryData(repository.publicEvents.value, repository.attendingEventIds.value, repository.interestedEventIds.value)
        }
    )
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.publicEvents, repository.attendingEventIds, repository.interestedEventIds, ::Triple)
                .collect { (events, attending, interested) ->
                    _uiState.update { it.withRepositoryData(events, attending, interested) }
                }
        }
    }

    fun toggleInterest() {
        val interested = repository.toggleInterest(eventId) ?: return
        showMessage(if (interested) "Guardado en tus eventos de interés" else "Quitado de tus eventos de interés")
    }

    /** Confirma la asistencia o, si ya estaba confirmada, la cancela. */
    fun toggleAttendance() {
        if (_uiState.value.isAttending) {
            if (repository.cancelAttendance(eventId)) showMessage("Cancelaste tu asistencia")
            return
        }
        when (repository.confirmAttendance(eventId)) {
            AttendanceResult.CONFIRMED -> showMessage("¡Asistencia confirmada! Te esperamos")
            AttendanceResult.ALREADY_CONFIRMED -> showMessage("Ya confirmaste tu asistencia a este evento")
            AttendanceResult.FULL -> showMessage(FULL_EVENT_MESSAGE)
            AttendanceResult.NOT_FOUND -> showMessage("Este evento ya no está disponible")
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    private fun showMessage(message: String) = _uiState.update { it.copy(userMessage = message) }

    private fun EventDetailUiState.withRepositoryData(
        events: List<CommunityEvent>,
        attending: Set<String>,
        interested: Set<String>
    ): EventDetailUiState {
        val event = events.find { it.id == eventId }
        return copy(
            event = event,
            organizerEventCount = event?.let { found -> events.count { it.organizerId == found.organizerId } } ?: 0,
            isAttending = eventId in attending,
            isInterested = eventId in interested
        )
    }

    companion object {
        const val FULL_EVENT_MESSAGE = "No hay cupos disponibles: el evento está lleno"
    }
}
