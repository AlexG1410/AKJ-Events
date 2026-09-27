package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

data class CommunityEvent(
    val id: String,
    val organizerId: String,
    val title: String,
    val category: EventCategory,
    val description: String,
    val address: String,
    val location: Location,
    val imageUrls: List<String>,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    val capacity: Int? = null,
    val attendanceCount: Int = 0,
    val interestCount: Int = 0,
    val status: EventStatus = EventStatus.PENDING,
    val rejectionReason: String? = null
) {
    init {
        require(imageUrls.isNotEmpty()) { "El evento debe tener al menos una imagen" }
        require(endsAt.isAfter(startsAt)) { "La fecha de fin debe ser posterior al inicio" }
        require(capacity == null || capacity > 0) { "El cupo debe ser positivo" }
        require(status != EventStatus.REJECTED || !rejectionReason.isNullOrBlank()) {
            "Un evento rechazado debe indicar el motivo"
        }
    }
}
