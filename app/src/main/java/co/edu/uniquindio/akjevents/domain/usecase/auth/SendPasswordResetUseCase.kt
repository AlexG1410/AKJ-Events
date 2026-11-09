package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Envía al correo el enlace para restablecer la contraseña; sirve para usuarios y moderadores. */
class SendPasswordResetUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String) = userRepository.sendPasswordReset(email.trim())
}
