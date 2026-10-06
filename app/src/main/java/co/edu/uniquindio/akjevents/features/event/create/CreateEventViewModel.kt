package co.edu.uniquindio.akjevents.features.event.create

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
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
    val limitCapacity: Boolean = false,
    val capacity: String = CreateEventViewModel.DEFAULT_CAPACITY.toString(),
    val imageSeed: String = "",
    val result: RequestResult? = null
) {
    // Aún no hay carga de imágenes: se usa una imagen temporal de picsum.photos
    val imageUrl: String get() = "https://picsum.photos/seed/$imageSeed/900/500"

    // Pasos del indicador superior (1. Info, 2. Fecha, 3. Cupos)
    val isInfoComplete: Boolean get() = title.isNotBlank() && category != null && description.isNotBlank()
    val isScheduleComplete: Boolean get() = startsAt != null && endsAt != null && place.isNotBlank()

    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

class CreateEventViewModel(
    private val repository: EventRepository = InMemoryEventRepository.shared,
    private val organizer: User = SampleUsers.currentUser,
    private val now: () -> LocalDateTime = LocalDateTime::now,
    private val random: Random = Random.Default
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateEventUiState(imageSeed = newImageSeed()))
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }

    fun selectCategory(category: EventCategory) = _uiState.update { it.copy(category = category) }

    fun onDescriptionChange(description: String) =
        _uiState.update { it.copy(description = description.take(MAX_DESCRIPTION_LENGTH)) }

    fun onStartsAtChange(startsAt: LocalDateTime) = _uiState.update { it.copy(startsAt = startsAt) }

    fun onEndsAtChange(endsAt: LocalDateTime) = _uiState.update { it.copy(endsAt = endsAt) }

    fun onPlaceChange(place: String) = _uiState.update { it.copy(place = place) }

    fun onLimitCapacityChange(limit: Boolean) = _uiState.update { it.copy(limitCapacity = limit) }

    // Solo dígitos, para que el cupo siempre se pueda convertir a número
    fun onCapacityChange(capacity: String) =
        _uiState.update { it.copy(capacity = capacity.filter(Char::isDigit).take(MAX_CAPACITY_DIGITS)) }

    fun increaseCapacity() = changeCapacityBy(1)

    fun decreaseCapacity() = changeCapacityBy(-1)

    fun changeImage() = _uiState.update { state ->
        var seed: String
        do seed = newImageSeed() while (seed == state.imageSeed)
        state.copy(imageSeed = seed)
    }

    fun submit() {
        val state = _uiState.value
        if (state.isBusy) return

        // Se valida aquí para que el constructor de CommunityEvent nunca falle por sus require
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error)) }
            return
        }

        val event = CommunityEvent(
            id = "evento-${UUID.randomUUID()}",
            organizerId = organizer.id,
            title = state.title.trim(),
            category = state.category!!,
            description = state.description.trim(),
            address = state.place.trim(),
            // Sin mapa en esta fase: se usa la ubicación del organizador hasta poder elegirla
            location = organizer.location,
            imageUrls = listOf(state.imageUrl),
            startsAt = state.startsAt!!,
            endsAt = state.endsAt!!,
            capacity = if (state.limitCapacity) state.capacity.toInt() else null,
            // Todo evento nuevo espera la revisión de un moderador antes de ser público
            status = EventStatus.PENDING
        )
        repository.createEvent(event)
        _uiState.update { it.copy(result = RequestResult.Success(SUBMITTED_MESSAGE)) }
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

    private fun newImageSeed(): String = "akj-${random.nextInt(100_000, 1_000_000)}"

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 600
        const val DEFAULT_CAPACITY = 30
        private const val MAX_CAPACITY_DIGITS = 5
        private const val MAX_CAPACITY = 99_999
        const val SUBMITTED_MESSAGE = "Tu evento fue enviado a verificación"
    }
}
