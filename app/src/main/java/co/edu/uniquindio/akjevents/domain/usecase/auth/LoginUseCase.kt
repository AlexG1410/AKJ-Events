package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Abre la sesión si el correo y la contraseña coinciden; devuelve null si no. */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): User? = userRepository.login(email.trim(), password)
}
