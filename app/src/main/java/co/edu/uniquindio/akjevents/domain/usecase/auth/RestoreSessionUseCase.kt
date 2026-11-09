package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Al abrir la app: espera a saber si quedó una sesión abierta y la cierra si la persona no pidió
 * «Recordar este dispositivo». Así la app entra directo solo cuando se eligió recordar.
 */
class RestoreSessionUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferencesRepository
) {
    suspend operator fun invoke() {
        userRepository.isSessionReady.first { it }
        if (userRepository.currentUser.value != null && !sessionPreferences.rememberDevice.first()) {
            userRepository.logout()
        }
    }
}
