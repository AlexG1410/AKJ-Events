package co.edu.uniquindio.akjevents.features.event.create

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.CreateEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.GetEventByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.UpdateEventUseCase
import co.edu.uniquindio.akjevents.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
@HiltViewModel
class CreateEventViewModel(
    private val getEventById: GetEventByIdUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val createEvent: CreateEventUseCase,
    private val updateEvent: UpdateEventUseCase,
    private val resources: ResourceProvider,
    private val eventId: String? = null,
    private val now: () -> LocalDateTime = LocalDateTime::now,
    private val random: Random = Random.Default
) : ViewModel() {

    /**
     * Hilt lee el id de la ruta [MainRoutes.EditEvent]; en [MainRoutes.CreateEvent] no hay id
     * y el formulario crea un evento nuevo.
     */
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        getEventById: GetEventByIdUseCase,
        observeCurrentUser: ObserveCurrentUserUseCase,
        createEvent: CreateEventUseCase,
        updateEvent: UpdateEventUseCase,
        resources: ResourceProvider
    ) : this(getEventById, observeCurrentUser, createEvent, updateEvent, resources, savedStateHandle.get<String>(EVENT_ID_KEY))

    // Al editar, el formulario no se puede enviar hasta cargar el evento
    private val _uiState = MutableStateFlow(
        CreateEventUiState(imageUrl = newImageUrl(), isEditing = eventId != null, canSubmit = eventId == null)
    )
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    init {
        if (eventId != null) {
            viewModelScope.launch {
                val event = runCatching { getEventById(eventId) }.getOrNull()
                _uiState.value = editState(event)
            }
        }
    }

    /** Estado del formulario con los datos del evento a editar, si el usuario puede editarlo. */
    private fun editState(event: CommunityEvent?): CreateEventUiState {
        val ownerId = observeCurrentUser().value?.id
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
        val organizer = observeCurrentUser().value
        if (organizer == null || !state.canSubmit) {
            _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(R.string.create_error_not_allowed))) }
            return
        }

        // Se valida aquí para que el constructor de CommunityEvent nunca falle por sus require
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(error))) }
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
        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            val result = runCatching {
                if (eventId == null) {
                    createEvent(event)
                    RequestResult.Success(resources.getString(R.string.create_submitted))
                } else {
                    // El cupo se compara con los asistentes que haya justo ahora, no con los de al abrir el formulario
                    val attendees = getEventById(eventId)?.attendanceCount ?: 0
                    when {
                        event.capacity != null && event.capacity < attendees -> RequestResult.Failure(resources.getString(R.string.create_error_capacity_below_attendance))
                        !updateEvent(event, organizer.id) -> RequestResult.Failure(resources.getString(R.string.create_error_save))
                        state.originalStatus == EventStatus.REJECTED -> RequestResult.Success(resources.getString(R.string.create_resubmitted))
                        else -> RequestResult.Success(resources.getString(R.string.create_updated))
                    }
                }
            }.getOrElse { RequestResult.Failure(resources.getString(R.string.create_error_network)) }
            _uiState.update { it.copy(result = result) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    /** Devuelve el mensaje de error (recurso de texto) o null si el formulario es válido. */
    @StringRes
    private fun validate(state: CreateEventUiState): Int? {
        val startsAt = state.startsAt
        val endsAt = state.endsAt
        return when {
            state.title.isBlank() -> R.string.create_error_title
            state.category == null -> R.string.create_error_category
            state.description.isBlank() -> R.string.create_error_description
            state.place.isBlank() -> R.string.create_error_place
            state.location == null -> R.string.create_error_location
            startsAt == null || endsAt == null -> R.string.create_error_dates
            !startsAt.isAfter(now()) -> R.string.create_error_past_start
            !endsAt.isAfter(startsAt) -> R.string.create_error_end_before_start
            state.limitCapacity && (state.capacity.toIntOrNull() ?: 0) <= 0 -> R.string.create_error_capacity
            else -> null
        }
    }

    private fun changeCapacityBy(delta: Int) = _uiState.update {
        val capacity = ((it.capacity.toIntOrNull() ?: 0) + delta).coerceIn(0, MAX_CAPACITY)
        it.copy(capacity = capacity.toString())
    }

    private fun newImageUrl(): String = "https://picsum.photos/seed/akj-${random.nextInt(100_000, 1_000_000)}/900/500"

    companion object {
        /** Nombre del argumento de [MainRoutes.EditEvent]. */
        private const val EVENT_ID_KEY = "eventId"
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 600
        const val DEFAULT_CAPACITY = 30
        private const val MAX_CAPACITY_DIGITS = 5
        private const val MAX_CAPACITY = 99_999
    }
}
