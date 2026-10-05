package co.edu.uniquindio.akjevents.core.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import co.edu.uniquindio.akjevents.core.util.RequestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tiempo que el Snackbar de confirmación se ve antes de cambiar de pantalla. */
private const val SUCCESS_NAVIGATION_DELAY_MS = 1_500L

/**
 * Muestra en un Snackbar el mensaje de un [RequestResult].
 * Con [RequestResult.Failure] avisa con [onFailureShown] para que el ViewModel limpie el resultado;
 * con [RequestResult.Success] deja ver la confirmación y luego llama a [onSuccess].
 */
@Composable
fun RequestResultEffect(
    result: RequestResult?,
    snackbarHostState: SnackbarHostState,
    onFailureShown: () -> Unit,
    onSuccess: () -> Unit
) {
    // El Snackbar se lanza en otro scope para que no se cancele cuando cambie el resultado
    val scope = rememberCoroutineScope()
    val currentOnFailureShown by rememberUpdatedState(onFailureShown)
    val currentOnSuccess by rememberUpdatedState(onSuccess)

    fun show(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(result) {
        when (result) {
            is RequestResult.Failure -> {
                show(result.errorMessage)
                currentOnFailureShown()
            }
            is RequestResult.Success -> {
                show(result.message)
                delay(SUCCESS_NAVIGATION_DELAY_MS)
                currentOnSuccess()
            }
            else -> Unit
        }
    }
}
