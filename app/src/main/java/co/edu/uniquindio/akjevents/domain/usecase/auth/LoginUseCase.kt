package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Abre la sesión si el correo y la contraseña coinciden; devuelve null si no.
 * Guarda si la persona pidió recordar el dispositivo (guía 13).
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferencesRepository
) {
    suspend operator fun invoke(email: String, password: String, rememberDevice: Boolean = false): User? {
        val user = userRepository.login(email.trim(), password) ?: return null
        sessionPreferences.setRememberDevice(rememberDevice)
        return user
    }
}
