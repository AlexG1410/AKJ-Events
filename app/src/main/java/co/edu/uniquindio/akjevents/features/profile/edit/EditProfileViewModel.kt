package co.edu.uniquindio.akjevents.features.profile.edit

import androidx.lifecycle.ViewModel
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EditProfileUiState(
    val user: User? = null,
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val address: String = "",
    val location: Location? = null,
    val interests: Set<EventCategory> = emptySet(),
    val result: RequestResult? = null,
    /** La cuenta se eliminó: la pantalla vuelve al Login. */
    val isAccountDeleted: Boolean = false
) {
    val nameError: String?
        get() = if (name.isNotEmpty() && name.trim().length < MIN_NAME_LENGTH) "Escribe un nombre de al menos $MIN_NAME_LENGTH caracteres" else null
    val emailError: String?
        get() = if (email.isNotEmpty() && !FormValidation.isValidEmail(email)) FormValidation.INVALID_EMAIL_MESSAGE else null
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success

    companion object {
        const val MIN_NAME_LENGTH = 3
    }
}

/** Mockup 13: datos personales, ubicación en el mapa, intereses y eliminación de la cuenta. */
class EditProfileViewModel(
    private val users: UserRepository = InMemoryUserRepository.shared,
    private val events: EventRepository = InMemoryEventRepository.shared
) : ViewModel() {
    val cities: List<String> = SamplePlaces.cities.map { it.name }

    private val _uiState = MutableStateFlow(
        users.currentUser.value?.let { user ->
            EditProfileUiState(
                user = user,
                name = user.name,
                email = user.email,
                city = user.city,
                address = user.address,
                location = user.location,
                interests = user.interests
            )
        } ?: EditProfileUiState()
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onCityChange(city: String) = _uiState.update {
        // Al cambiar de ciudad la ubicación se mueve a su centro hasta que se elija un punto en el mapa
        val cityLocation = SamplePlaces.cityNamed(city)?.location ?: it.location
        it.copy(city = city, location = cityLocation, address = city)
    }

    fun onLocationPicked(picked: PickedLocation) = _uiState.update {
        it.copy(location = picked.location, address = picked.address, city = SamplePlaces.nearestCity(picked.location).name)
    }

    fun toggleInterest(category: EventCategory) = _uiState.update {
        it.copy(interests = if (category in it.interests) it.interests - category else it.interests + category)
    }

    fun save() {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isBusy) return
        val error = when {
            state.name.trim().length < EditProfileUiState.MIN_NAME_LENGTH -> "Escribe un nombre de al menos ${EditProfileUiState.MIN_NAME_LENGTH} caracteres"
            !FormValidation.isValidEmail(state.email) -> FormValidation.INVALID_EMAIL_MESSAGE
            state.city.isBlank() || state.location == null -> "Elige tu ubicación principal"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error)) }
            return
        }
        val updated = user.copy(
            name = state.name,
            email = state.email,
            city = state.city,
            address = state.address.ifBlank { state.city },
            location = state.location!!,
            interests = state.interests
        )
        val result = when (users.updateProfile(updated)) {
            ProfileUpdateResult.UPDATED -> RequestResult.Success("Perfil actualizado")
            ProfileUpdateResult.EMAIL_IN_USE -> RequestResult.Failure("Ese correo ya pertenece a otra cuenta")
            ProfileUpdateResult.NOT_FOUND -> RequestResult.Failure("No encontramos tu cuenta")
        }
        _uiState.update { it.copy(result = result) }
    }

    /** Borra la cuenta junto con sus eventos, comentarios y participación, y cierra la sesión. */
    fun deleteAccount() {
        val user = _uiState.value.user ?: return
        events.removeUserData(user.id)
        if (users.deleteAccount(user.id)) {
            _uiState.update { it.copy(isAccountDeleted = true) }
        } else {
            _uiState.update { it.copy(result = RequestResult.Failure("No se pudo eliminar la cuenta")) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }
}
