package co.edu.uniquindio.akjevents.domain.usecase.user

import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Guarda los datos personales; el rol, los puntos y las insignias no cambian. */
class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(updated: User): ProfileUpdateResult = userRepository.updateProfile(updated)
}
