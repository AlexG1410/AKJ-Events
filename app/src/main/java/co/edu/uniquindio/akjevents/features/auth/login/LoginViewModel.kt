package co.edu.uniquindio.akjevents.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberDevice: Boolean = false,
    val result: RequestResult? = null,
    // Define a qué pantalla se entra después de iniciar sesión
    val isModerator: Boolean = false
) {
    val isEmailValid: Boolean get() = FormValidation.isValidEmail(email)
    val emailError: String?
        get() = if (email.isNotBlank() && !isEmailValid) FormValidation.INVALID_EMAIL_MESSAGE else null
    val passwordError: String?
        get() = if (password.isNotEmpty() && !FormValidation.isValidPassword(password)) {
            FormValidation.SHORT_PASSWORD_MESSAGE
        } else null
    val isFormValid: Boolean get() = isEmailValid && FormValidation.isValidPassword(password)
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

class LoginViewModel(
    private val users: UserRepository = InMemoryUserRepository.shared,
    private val simulatedRequestMs: Long = SIMULATED_REQUEST_MS
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password) }

    fun togglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onRememberDeviceChange(remember: Boolean) = _uiState.update { it.copy(rememberDevice = remember) }

    fun login() {
        val state = _uiState.value
        if (state.isBusy) return

        val error = when {
            state.email.isBlank() || state.password.isBlank() -> "Completa el correo y la contraseña"
            !state.isEmailValid -> FormValidation.INVALID_EMAIL_MESSAGE
            !FormValidation.isValidPassword(state.password) -> FormValidation.SHORT_PASSWORD_MESSAGE
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error)) }
            return
        }

        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            // Las cuentas viven en memoria; con el backend las validará el servicio de autenticación
            delay(simulatedRequestMs)
            val user = users.login(state.email, state.password)
            val result = if (user != null) {
                RequestResult.Success("¡Bienvenido de nuevo, ${user.firstName}!")
            } else {
                RequestResult.Failure("Correo o contraseña incorrectos")
            }
            _uiState.update { it.copy(result = result, isModerator = user?.isModerator == true) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    companion object {
        const val SIMULATED_REQUEST_MS = 800L
    }
}
