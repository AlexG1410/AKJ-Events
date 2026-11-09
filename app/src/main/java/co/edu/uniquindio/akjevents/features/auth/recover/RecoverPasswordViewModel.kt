package co.edu.uniquindio.akjevents.features.auth.recover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.util.FormValidation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.core.util.ResourceProvider
import co.edu.uniquindio.akjevents.core.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecoverPasswordUiState(
    val email: String = "",
    val result: RequestResult? = null
) {
    val emailError: UiText?
        get() = if (email.isNotBlank() && !FormValidation.isValidEmail(email)) {
            FormValidation.INVALID_EMAIL_MESSAGE
        } else null
    val isFormValid: Boolean get() = FormValidation.isValidEmail(email)
    val isBusy: Boolean get() = result is RequestResult.Loading || result is RequestResult.Success
}

@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val resources: ResourceProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }

    fun sendRecoveryLink() {
        val state = _uiState.value
        if (state.isBusy) return

        val error = when {
            state.email.isBlank() -> UiText(R.string.recover_error_empty)
            !FormValidation.isValidEmail(state.email) -> FormValidation.INVALID_EMAIL_MESSAGE
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(result = RequestResult.Failure(error.asString(resources))) }
            return
        }

        _uiState.update { it.copy(result = RequestResult.Loading) }
        viewModelScope.launch {
            // Simulación: todavía no se envían correos reales
            delay(SIMULATED_REQUEST_MS)
            _uiState.update { it.copy(result = RequestResult.Success(resources.getString(R.string.recover_success))) }
        }
    }

    fun onFailureShown() = _uiState.update {
        if (it.result is RequestResult.Failure) it.copy(result = null) else it
    }

    companion object {
        const val SIMULATED_REQUEST_MS = 700L
    }
}
