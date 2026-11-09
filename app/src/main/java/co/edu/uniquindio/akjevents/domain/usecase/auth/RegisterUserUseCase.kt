package co.edu.uniquindio.akjevents.domain.usecase.auth

import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Crea la cuenta siempre con el rol Usuario: los moderadores están precargados. */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult = userRepository.register(name, email, city, password, interests, address, location)
}
