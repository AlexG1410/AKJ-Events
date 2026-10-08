package co.edu.uniquindio.akjevents.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PasswordStrength(val label: String, val filledBars: Int) {
    NONE("", 0),
    WEAK("Débil", 1),
    MEDIUM("Media", 2),
    STRONG("Segura", 3);

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
    val fullNameError: String?
        get() = if (fullName.isNotBlank() && fullName.trim().length < RegisterViewModel.MIN_NAME_LENGTH) {
            "Escribe un nombre de al menos ${RegisterViewModel.MIN_NAME_LENGTH} caracteres"
        } else null
    val emailError: String?
        get() = if (email.isNotBlank() && !FormValidation.isValidEmail(email)) {
            FormValidation.INVALID_EMAIL_MESSAGE
        } else null
    val passwordError: String?
        get() = if (password.isNotEmpty() && !FormValidation.isStrongPassword(password)) {
            FormValidation.WEAK_PASSWORD_MESSAGE
        } else null
    val confirmPasswordError: String?
        get() = if (confirmPassword.isNotEmpty() && confirmPassword != password) {
            "Las contraseñas no coinciden"
        } else null
    val isFormValid: Boolean
        get() = fullName.trim().length >= RegisterViewModel.MIN_NAME_LENGTH &&
            FormValidation.isValidEmail(email) &&
            city.isNotBlank() &&
            FormValidation.isStrongPassword(password) &&
            confirmPassword == password &&
            acceptedRules
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

class RegisterViewModel(
    private val simulatedRequestMs: Long = SIMULATED_REQUEST_MS
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    val cities = listOf(
        "Armenia, Quindío",
        "Calarcá, Quindío",
        "Circasia, Quindío",
        "Montenegro, Quindío",
        "Pereira, Risaralda",
        "Manizales, Caldas"
    )

    fun onFullNameChange(name: String) = _uiState.update { it.copy(fullName = name) }

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onCityChange(city: String) = _uiState.update { it.copy(city = city) }

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
                "Completa los campos obligatorios (*)"
            state.fullName.trim().length < MIN_NAME_LENGTH ->
                "Escribe un nombre de al menos $MIN_NAME_LENGTH caracteres"
            !FormValidation.isValidEmail(state.email) -> FormValidation.INVALID_EMAIL_MESSAGE
            !FormValidation.isStrongPassword(state.password) -> FormValidation.WEAK_PASSWORD_MESSAGE
            state.confirmPassword != state.password -> "Las contraseñas no coinciden"
            !state.acceptedRules -> "Debes aceptar las Normas Comunitarias para continuar"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error)) }
            return
        }

        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            // Simulación: todavía no hay backend de registro
            delay(simulatedRequestMs)
            _uiState.update {
                it.copy(result = RequestResult.Success("¡Cuenta creada! Empiezas como ${it.startingLevel.label} (Nivel 1)"))
            }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val SIMULATED_REQUEST_MS = 1_000L
    }
}
