package co.edu.uniquindio.akjevents.domain.usecase.event

import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import javax.inject.Inject

/** Aviso del organizador a sus asistentes. Devuelve a cuántas personas les llegó. */
class SendNoticeUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(eventId: String, organizerId: String, message: String): Int {
        val event = eventRepository.findEvent(eventId)
        if (event == null || event.organizerId != organizerId || message.isBlank()) return 0
        val recipients = eventRepository.attendances.value
            .filter { it.eventId == eventId && it.userId != organizerId }
            .map { it.userId }
            .distinct()
        recipients.forEach {
            notificationRepository.notify(it, NotificationType.ORGANIZER_NOTICE, eventId = eventId, eventTitle = event.title, detail = message.trim())
        }
        return recipients.size
    }
}
