package co.edu.uniquindio.akjevents.domain.usecase.participation

import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import javax.inject.Inject

/**
 * Comentario en un evento verificado o finalizado. Suma puntos al autor del comentario
 * y notifica al organizador del evento, salvo que el comentario sea suyo.
 */
class AddCommentUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val awardPoints: AwardPointsUseCase,
    private val awardBadge: AwardBadgeUseCase
) {
    suspend operator fun invoke(eventId: String, authorId: String, text: String): Comment? {
        val event = eventRepository.findEvent(eventId) ?: return null
        val canComment = event.status == EventStatus.VERIFIED || event.status == EventStatus.FINISHED
        val author = userRepository.findById(authorId)
        if (!canComment || text.isBlank() || author == null) return null

        val comment = eventRepository.addComment(eventId, authorId, text.trim())
        awardPoints(authorId, ReputationPoints.COMMENT, "Comentaste en «${event.title}»")
        if (eventRepository.comments.value.count { it.authorId == authorId } == 1) awardBadge(authorId, Badge.FIRST_COMMENT)
        if (authorId != event.organizerId) {
            notificationRepository.notify(
                userId = event.organizerId,
                type = NotificationType.NEW_COMMENT,
                title = "Nuevo comentario en tu evento",
                message = "${author.name} comentó en «${event.title}»: «${comment.text}»",
                eventId = eventId
            )
        }
        return comment
    }
}
