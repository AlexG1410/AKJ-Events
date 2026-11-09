package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

/** Usuario con la sesión abierta; su valor actual también sirve para consultas puntuales. */
class ObserveCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): StateFlow<User?> = userRepository.currentUser
}
