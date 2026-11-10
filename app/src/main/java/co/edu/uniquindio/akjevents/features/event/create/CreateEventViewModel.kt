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
import co.edu.uniquindio.akjevents.domain.repository.ImageFolder
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.CreateEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.GetEventByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.UpdateEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.image.UploadImagesUseCase
import co.edu.uniquindio.akjevents.navigation.MainRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
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
    /**
     * Fotos del evento; la primera es la principal. Las ya publicadas son URLs y las nuevas son URIs
     * locales de la cámara o la galería, que se suben a la nube al enviar el formulario.
     */
    val images: List<String> = emptyList(),
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

    val canAddImage: Boolean get() = images.size < CreateEventViewModel.MAX_IMAGES
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
    private val uploadImages: UploadImagesUseCase,
    private val resources: ResourceProvider,
    private val eventId: String? = null,
    private val now: () -> LocalDateTime = LocalDateTime::now
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
        uploadImages: UploadImagesUseCase,
        resources: ResourceProvider
    ) : this(
        getEventById, observeCurrentUser, createEvent, updateEvent, uploadImages, resources, savedStateHandle.get<String>(EVENT_ID_KEY)
    )

    // Al editar, el formulario no se puede enviar hasta cargar el evento
    private val _uiState = MutableStateFlow(
        CreateEventUiState(isEditing = eventId != null, canSubmit = eventId == null)
    )
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()
    private var lastMapAddress: String? = null

    init {
        if (eventId != null) {
            viewModelScope.launch {
                val event = runCatching { getEventById(eventId) }.getOrNull()
                _uiState.value = editState(event)
                lastMapAddress = event?.address
            }
        }
    }

    /** Estado del formulario con los datos del evento a editar, si el usuario puede editarlo. */
    private fun editState(event: CommunityEvent?): CreateEventUiState {
        val ownerId = observeCurrentUser().value?.id
        if (event == null || event.organizerId != ownerId || event.status == EventStatus.FINISHED) {
            return CreateEventUiState(isEditing = true, canSubmit = false)
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
            images = event.imageUrls,
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

    /** Actualiza la dirección automática al reseleccionar, pero respeta un punto de encuentro escrito. */
    fun onLocationPicked(picked: PickedLocation) {
        _uiState.update {
            it.copy(location = picked.location, place = if (it.place.isBlank() || it.place == lastMapAddress) picked.address else it.place)
        }
        lastMapAddress = picked.address
    }

    fun onLimitCapacityChange(limit: Boolean) = _uiState.update { it.copy(limitCapacity = limit) }

    // Solo dígitos, para que el cupo siempre se pueda convertir a número
    fun onCapacityChange(capacity: String) =
        _uiState.update { it.copy(capacity = capacity.filter(Char::isDigit).take(MAX_CAPACITY_DIGITS)) }

    fun increaseCapacity() = changeCapacityBy(1)

    fun decreaseCapacity() = changeCapacityBy(-1)

    /** Foto tomada con la cámara o elegida de la galería; se sube al enviar el formulario. */
    fun addImage(uri: String) = _uiState.update {
        if (it.canAddImage && uri !in it.images) it.copy(images = it.images + uri) else it
    }

    fun removeImage(index: Int) = _uiState.update {
        if (index in it.images.indices) it.copy(images = it.images.filterIndexed { i, _ -> i != index }) else it
    }

    fun submit() {
        val state = _uiState.value
        if (state.isBusy) return
        val organizer = observeCurrentUser().value
        if (organizer == null) {
            _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(R.string.create_error_session_unavailable))) }
            return
        }
        if (!state.canSubmit) {
            _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(R.string.create_error_not_allowed))) }
            return
        }

        // Se valida aquí para que el constructor de CommunityEvent nunca falle por sus require
        val error = validate(state)
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(error))) }
            return
        }

        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            // Primero se suben las fotos nuevas; sin ellas el evento no se guarda
            val imageUrls = runCatching { uploadImages(state.images, ImageFolder.EVENTS) }.getOrElse {
                _uiState.update { it.copy(result = RequestResult.Failure(resources.getString(R.string.create_error_upload))) }
                return@launch
            }
            _uiState.update { it.copy(images = imageUrls) }
            save(state, organizer.id, imageUrls)
        }
    }

    private suspend fun save(state: CreateEventUiState, organizerId: String, imageUrls: List<String>) {
        val event = CommunityEvent(
            id = eventId ?: "evento-${UUID.randomUUID()}",
            organizerId = organizerId,
            title = state.title.trim(),
            category = state.category!!,
            description = state.description.trim(),
            address = state.place.trim(),
            location = state.location!!,
            imageUrls = imageUrls,
            startsAt = state.startsAt!!,
            endsAt = state.endsAt!!,
            capacity = if (state.limitCapacity) state.capacity.toInt() else null,
            // Todo evento nuevo espera la revisión de un moderador antes de ser público
            status = EventStatus.PENDING,
            createdAt = now()
        )
        val result = runCatching {
            if (eventId == null) {
                createEvent(event)
                RequestResult.Success(resources.getString(R.string.create_submitted))
            } else {
                // El cupo se compara con los asistentes que haya justo ahora, no con los de al abrir el formulario
                val attendees = getEventById(eventId)?.attendanceCount ?: 0
                when {
                    event.capacity != null && event.capacity < attendees -> RequestResult.Failure(resources.getString(R.string.create_error_capacity_below_attendance))
                    !updateEvent(event, organizerId) -> RequestResult.Failure(resources.getString(R.string.create_error_save))
                    state.originalStatus == EventStatus.REJECTED -> RequestResult.Success(resources.getString(R.string.create_resubmitted))
                    else -> RequestResult.Success(resources.getString(R.string.create_updated))
                }
            }
        }.getOrElse { RequestResult.Failure(resources.getString(R.string.create_error_network)) }
        _uiState.update { it.copy(result = result) }
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
            state.images.isEmpty() -> R.string.create_error_image
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

    companion object {
        /** Nombre del argumento de [MainRoutes.EditEvent]. */
        private const val EVENT_ID_KEY = "eventId"
        const val MAX_TITLE_LENGTH = 80
        const val MAX_DESCRIPTION_LENGTH = 600
        const val DEFAULT_CAPACITY = 30
        /** Fotos por evento, como en el mockup 07 ("1 de 3"). */
        const val MAX_IMAGES = 3
        private const val MAX_CAPACITY_DIGITS = 5
        private const val MAX_CAPACITY = 99_999
    }
}
