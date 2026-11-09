package co.edu.uniquindio.akjevents.domain.usecase.reputation

import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Suma (o resta) puntos de reputación y avisa cuando el usuario sube de nivel. */
class AwardPointsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, points: Int, reason: String) {
        if (points == 0) return
        // Los moderadores no participan del sistema de reputación
        val user = userRepository.findById(userId) ?: return
        if (user.isModerator) return
        val change = userRepository.addPoints(userId, points, reason) ?: return
        val level = change.after.level
        if (level.ordinal > change.before.level.ordinal) {
            notificationRepository.notify(
                userId = userId,
                type = NotificationType.LEVEL_UP,
                title = "¡Ascendiste a ${level.label}!",
                message = "Alcanzaste ${level.minPoints} puntos cívicos y ahora eres nivel ${level.number}: ${level.label}."
            )
        }
    }
}
