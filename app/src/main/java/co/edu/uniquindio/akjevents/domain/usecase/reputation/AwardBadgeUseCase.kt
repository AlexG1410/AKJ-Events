package co.edu.uniquindio.akjevents.domain.usecase.reputation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import javax.inject.Inject

/** Otorga una insignia una sola vez y la notifica. */
class AwardBadgeUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String, badge: Badge) {
        val user = userRepository.findById(userId) ?: return
        if (user.isModerator || !userRepository.addBadge(userId, badge)) return
        notificationRepository.notify(
            userId = userId,
            type = NotificationType.BADGE_UNLOCKED,
            title = "Nueva insignia: ${badge.label}",
            message = "${badge.description}. ¡Sigue participando!"
        )
    }
}
