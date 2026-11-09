package co.edu.uniquindio.akjevents.features.auth.register

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.core.util.UiText
import co.edu.uniquindio.akjevents.core.util.labelRes
import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.usecase.auth.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PasswordStrength(@param:StringRes val label: Int, val filledBars: Int) {
    // NONE no se muestra: el indicador solo aparece cuando hay contraseña
    NONE(R.string.password_strength_weak, 0),
    WEAK(R.string.password_strength_weak, 1),
    MEDIUM(R.string.password_strength_medium, 2),
    STRONG(R.string.password_strength_strong, 3);

    companion object {
        fun of(password: String): PasswordStrength {
            if (password.isEmpty()) return NONE
            val checks = listOf(
                FormValidation.isValidPassword(password),
                password.any(Char::isUpperCase),
                password.any(Char::isDigit)
            )
            return when (checks.count { it }) {
                3 -> STRONG
                2 -> MEDIUM
                else -> WEAK
            }
        }
    }
}

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val city: String = "",
    val address: String = "",
    val location: Location? = null,
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val interests: Set<EventCategory> = emptySet(),
    val acceptedRules: Boolean = false,
    val result: RequestResult? = null
) {
    // Todo usuario nuevo empieza como Espectador (nivel 1)
    val startingLevel: UserLevel get() = UserLevel.SPECTATOR
    val passwordStrength: PasswordStrength get() = PasswordStrength.of(password)
    val fullNameError: UiText?
        get() = if (fullName.isNotBlank() && fullName.trim().length < RegisterViewModel.MIN_NAME_LENGTH) {
            UiText(R.string.error_short_name, RegisterViewModel.MIN_NAME_LENGTH)
        } else null
    val emailError: UiText?
        get() = if (email.isNotBlank() && !FormValidation.isValidEmail(email)) {
            FormValidation.INVALID_EMAIL_MESSAGE
        } else null
    val passwordError: UiText?
        get() = if (password.isNotEmpty() && !FormValidation.isStrongPassword(password)) {
            FormValidation.WEAK_PASSWORD_MESSAGE
        } else null
    val confirmPasswordError: UiText?
        get() = if (confirmPassword.isNotEmpty() && confirmPassword != password) {
            UiText(R.string.error_passwords_mismatch)
        } else null
    val isFormValid: Boolean
        get() = fullName.trim().length >= RegisterViewModel.MIN_NAME_LENGTH &&
            FormValidation.isValidEmail(email) &&
            city.isNotBlank() &&
            address.isNotBlank() && location != null &&
            FormValidation.isStrongPassword(password) &&
            confirmPassword == password &&
            acceptedRules
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUser: RegisterUserUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    val cities: List<String> = SamplePlaces.cities.map { it.name }

    fun onFullNameChange(name: String) = _uiState.update { it.copy(fullName = name) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onCityChange(city: String) = _uiState.update {
        // Una ciudad distinta requiere confirmar de nuevo la dirección en el mapa.
        if (city == it.city) it else it.copy(city = city, address = "", location = null)
    }

    fun locationForPicker(): PickedLocation {
        val state = _uiState.value
        val center = state.location ?: SamplePlaces.cityNamed(state.city)?.location ?: SamplePlaces.cities.first().location
        return PickedLocation(state.address, center)
    }

    fun onLocationPicked(picked: PickedLocation) = _uiState.update {
        it.copy(address = picked.address, location = picked.location, city = SamplePlaces.cityForLocation(picked.location).name)
    }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun onConfirmPasswordChange(password: String) = _uiState.update { it.copy(confirmPassword = password) }

    fun togglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun toggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun toggleInterest(category: EventCategory) = _uiState.update {
        it.copy(interests = if (category in it.interests) it.interests - category else it.interests + category)
    }

    fun onAcceptedRulesChange(accepted: Boolean) = _uiState.update { it.copy(acceptedRules = accepted) }

    fun register() {
        val state = _uiState.value
        if (state.isBusy) return

        val error = when {
            listOf(state.fullName, state.email, state.city, state.password, state.confirmPassword).any { it.isBlank() } ->
                UiText(R.string.register_error_required)
            state.fullName.trim().length < MIN_NAME_LENGTH -> UiText(R.string.error_short_name, MIN_NAME_LENGTH)
            !FormValidation.isValidEmail(state.email) -> FormValidation.INVALID_EMAIL_MESSAGE
            state.location == null || state.address.isBlank() -> UiText(R.string.register_error_location)
            !FormValidation.isStrongPassword(state.password) -> FormValidation.WEAK_PASSWORD_MESSAGE
            state.confirmPassword != state.password -> UiText(R.string.error_passwords_mismatch)
            !state.acceptedRules -> UiText(R.string.register_error_rules)
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error.asString(resources))) }
            return
        }

        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            // La cuenta se guarda en memoria; con el backend la creará el servicio de autenticación
            val result = runCatching {
                registerUser(
                    name = state.fullName,
                    email = state.email,
                    city = state.city,
                    password = state.password,
                    interests = state.interests,
                    address = state.address,
                    location = state.location!!
                )
            }.fold(
                onSuccess = { outcome ->
                    when (outcome) {
                        is RegisterResult.Success ->
                            RequestResult.Success(resources.getString(R.string.register_success, resources.getString(state.startingLevel.labelRes)))
                        RegisterResult.EmailInUse -> RequestResult.Failure(resources.getString(R.string.register_error_email_in_use))
                    }
                },
                onFailure = { RequestResult.Failure(resources.getString(R.string.register_error_generic)) }
            )
            _uiState.update { it.copy(result = result) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
    }
}
