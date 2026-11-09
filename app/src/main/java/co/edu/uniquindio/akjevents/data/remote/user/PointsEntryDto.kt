package co.edu.uniquindio.akjevents.data.remote.user

import androidx.annotation.Keep
import co.edu.uniquindio.akjevents.data.remote.toLocalDateTime
import co.edu.uniquindio.akjevents.data.remote.toTimestamp
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import com.google.firebase.Timestamp
import java.time.LocalDateTime

/** Movimiento de puntos en la colección `pointsHistory`; guarda el motivo, no la frase (se traduce al mostrarlo). */
@Keep
data class PointsEntryDto(
    val userId: String = "",
    val points: Int = 0,
    val reason: String = PointsReason.ACCUMULATED.name,
    val eventTitle: String? = null,
    val createdAt: Timestamp? = null
) {
    fun toDomain(): PointsEntry = PointsEntry(
        userId = userId,
        points = points,
        reason = runCatching { PointsReason.valueOf(reason) }.getOrDefault(PointsReason.ACCUMULATED),
        createdAt = createdAt?.toLocalDateTime() ?: LocalDateTime.now(),
        eventTitle = eventTitle
    )

    companion object {
        fun fromDomain(entry: PointsEntry) = PointsEntryDto(
            userId = entry.userId,
            points = entry.points,
            reason = entry.reason.name,
            eventTitle = entry.eventTitle,
            createdAt = entry.createdAt.toTimestamp()
        )
    }
}
