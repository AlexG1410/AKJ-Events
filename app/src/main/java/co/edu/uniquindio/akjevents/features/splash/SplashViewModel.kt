package co.edu.uniquindio.akjevents.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashUiState(
    val isFinished: Boolean = false
)

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        // Al vivir en el ViewModel, la espera no se reinicia si se rota la pantalla
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    companion object {
        const val SPLASH_DURATION_MS = 2_000L
    }
}
