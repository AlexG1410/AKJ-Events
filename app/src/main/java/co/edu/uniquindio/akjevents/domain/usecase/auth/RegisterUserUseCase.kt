package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Crea la cuenta siempre con el rol Usuario: los moderadores están precargados.
 * Quien acaba de registrarse entra a la app y queda recordado en el dispositivo.
 */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferencesRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult {
        val result = userRepository.register(name, email, city, password, interests, address, location)
        if (result is RegisterResult.Success) sessionPreferences.setRememberDevice(true)
        return result
    }
}
