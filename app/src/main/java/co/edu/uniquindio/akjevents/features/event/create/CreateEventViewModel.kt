package co.edu.uniquindio.akjevents.features.event.create

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

data class CreateEventUiState(
    val title: String = "",
    val category: EventCategory? = null,
    val description: String = "",
    val startsAt: LocalDateTime? = null,
    val endsAt: LocalDateTime? = null,
    val place: String = "",
    val location: Location? = null,
    val limitCapacity: Boolean = false,
    val capacity: String = CreateEventViewModel.DEFAULT_CAPACITY.toString(),
    // Aún no hay carga de imágenes: se usa una imagen temporal de picsum.photos
    val imageUrl: String = "",
    /** Edición de un evento existente (mockup 08) en lugar de creación (mockup 07). */
    val isEditing: Boolean = false,
    /** Estado del evento antes de editarlo; un rechazado se "reenvía". */
    val originalStatus: EventStatus? = null,
    /** False si el evento a editar no existe o no pertenece al usuario. */
    val canSubmit: Boolean = true,
    val result: RequestResult? = null
) {
    // Pasos del indicador superior (1. Info, 2. Fecha, 3. Cupos)
    val isInfoComplete: Boolean get() = title.isNotBlank() && category != null && description.isNotBlank()
    val isScheduleComplete: Boolean get() = startsAt != null && endsAt != null && place.isNotBlank() && location != null

    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

/**
 * Formulario para crear un evento o, con [eventId], editar uno propio.
 * Todo evento creado o editado queda pendiente de verificación por un moderador.
 */
class CreateEventViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val users: UserRepository = InMemoryUserRepository.shared,
    private val eventId: String? = null,
    private val now: () -> LocalDateTime = LocalDateTime::now,
    private val random: Random = Random.Default
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    private fun initialState(): CreateEventUiState {
        if (eventId == null) return CreateEventUiState(imageUrl = newImageUrl())
        val event = repository.findEvent(eventId)
        val ownerId = users.currentUser.value?.id
        if (event == null || event.organizerId != ownerId || event.status == EventStatus.FINISHED) {
            return CreateEventUiState(isEditing = true, canSubmit = false, imageUrl = newImageUrl())
        }
        return CreateEventUiState(
            title = event.title,
            category = event.category,
            description = event.description,
            startsAt = event.startsAt,
            endsAt = event.endsAt,
            place = event.address,
            location = event.location,
            limitCapacity = event.capacity != null,
            capacity = (event.capacity ?: DEFAULT_CAPACITY).toString(),
            imageUrl = event.imageUrls.first(),
            isEditing = true,
            originalStatus = event.status
        )
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title.take(MAX_TITLE_LENGTH)) }

    fun selectCategory(category: EventCategory) = _uiState.update { it.copy(category = category) }

    fun onDescriptionChange(description: String) =
        _uiState.update { it.copy(description = description.take(MAX_DESCRIPTION_LENGTH)) }

    fun onStartsAtChange(startsAt: LocalDateTime) = _uiState.update { it.copy(startsAt = startsAt) }

    fun onEndsAtChange(endsAt: LocalDateTime) = _uiState.update { it.copy(endsAt = endsAt) }

    fun onPlaceChange(place: String) = _uiState.update { it.copy(place = place) }

    /** Punto elegido en el mapa; si aún no hay lugar escrito, se usa la dirección del mapa. */
    fun onLocationPicked(picked: PickedLocation) = _uiState.update {
        it.copy(location = picked.location, place = it.place.ifBlank { picked.address })
    }

    fun onLimitCapacityChange(limit: Boolean) = _uiState.update { it.copy(limitCapacity = limit) }

    // Solo dígitos, para que el cupo siempre se pueda convertir a número
    fun onCapacityChange(capacity: String) =
        _uiState.update { it.copy(capacity = capacity.filter(Char::isDigit).take(MAX_CAPACITY_DIGITS)) }

    fun increaseCapacity() = changeCapacityBy(1)

    fun decreaseCapacity() = changeCapacityBy(-1)

    fun changeImage() = _uiState.update { state ->
        var url: String
        do url = newImageUrl() while (url == state.imageUrl)
        state.copy(imageUrl = url)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isBusy) return
        val organizer = users.currentUser.value
        if (organizer == null || !state.canSubmit) {
            _uiState.update { it.copy(result = RequestResult.Failure("No puedes modificar este evento")) }
            return
        }

        // Se valida aquí para que el constructor de CommunityEvent nunca falle por sus require
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error)) }
            return
        }

        val event = CommunityEvent(
            id = eventId ?: "evento-${UUID.randomUUID()}",
            organizerId = organizer.id,
            title = state.title.trim(),
            category = state.category!!,
            description = state.description.trim(),
            address = state.place.trim(),
            location = state.location!!,
            imageUrls = listOf(state.imageUrl),
            startsAt = state.startsAt!!,
            endsAt = state.endsAt!!,
            capacity = if (state.limitCapacity) state.capacity.toInt() else null,
            // Todo evento nuevo espera la revisión de un moderador antes de ser público
            status = EventStatus.PENDING,
            createdAt = now()
        )
        val message = if (eventId == null) {
            repository.createEvent(event)
            SUBMITTED_MESSAGE
        } else {
            if (!repository.updateEvent(event, organizer.id)) {
                _uiState.update { it.copy(result = RequestResult.Failure("No se pudieron guardar los cambios")) }
                return
            }
            if (state.originalStatus == EventStatus.REJECTED) RESUBMITTED_MESSAGE else UPDATED_MESSAGE
        }
        _uiState.update { it.copy(result = RequestResult.Success(message)) }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    private fun validate(state: CreateEventUiState): String? {
        val startsAt = state.startsAt
        val endsAt = state.endsAt
        return when {
            state.title.isBlank() -> "Escribe el título del evento"
            state.category == null -> "Elige una categoría"
            state.description.isBlank() -> "Escribe la descripción del evento"
            state.place.isBlank() -> "Indica el lugar o punto de encuentro"
            state.location == null -> "Elige la ubicación del evento en el mapa"
            startsAt == null || endsAt == null -> "Elige la fecha y hora de inicio y de fin"
            !startsAt.isAfter(now()) -> "La fecha de inicio debe ser futura"
            !endsAt.isAfter(startsAt) -> "La fecha de fin debe ser posterior al inicio"
            state.limitCapacity && (state.capacity.toIntOrNull() ?: 0) <= 0 -> "El cupo máximo debe ser mayor que 0"
            else -> null
        }
    }

    private fun changeCapacityBy(delta: Int) = _uiState.update {
        val capacity = ((it.capacity.toIntOrNull() ?: 0) + delta).coerceIn(0, MAX_CAPACITY)
        it.copy(capacity = capacity.toString())
    }

    private fun newImageUrl(): String = "https://picsum.photos/seed/akj-${random.nextInt(100_000, 1_000_000)}/900/500"

    companion object {
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 600
        const val DEFAULT_CAPACITY = 30
        private const val MAX_CAPACITY_DIGITS = 5
        private const val MAX_CAPACITY = 99_999
        const val SUBMITTED_MESSAGE = "Tu evento fue enviado a verificación"
        const val UPDATED_MESSAGE = "Cambios guardados: el evento vuelve a revisión"
        const val RESUBMITTED_MESSAGE = "Evento corregido y reenviado a verificación"
    }
}
