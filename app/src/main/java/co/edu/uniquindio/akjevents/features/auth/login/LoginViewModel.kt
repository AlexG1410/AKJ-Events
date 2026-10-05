package co.edu.uniquindio.akjevents.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.RequestResult
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
    val result: RequestResult? = null
) {
    val isEmailValid: Boolean get() = FormValidation.isValidEmail(email)
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

class LoginViewModel : ViewModel() {
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
            // Simulación: todavía no hay backend de autenticación
            delay(SIMULATED_REQUEST_MS)
            _uiState.update { it.copy(result = RequestResult.Success("¡Bienvenido de nuevo! Iniciaste sesión")) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    companion object {
        const val SIMULATED_REQUEST_MS = 800L
    }
}
