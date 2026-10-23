package co.edu.uniquindio.akjevents.domain.model

import java.time.LocalDateTime

/** Puntos que otorga cada forma de participación. */
object ReputationPoints {
    const val CREATE_EVENT = 10
    const val EVENT_VERIFIED = 20
    const val EVENT_FINISHED = 10
    const val COMMENT = 5
    const val INTEREST_RECEIVED = 2
    const val CHECK_IN = 15

    /** Eventos verificados necesarios para la insignia [Badge.TEN_VERIFIED]. */
    const val VERIFIED_FOR_BADGE = 10
}

/** Movimiento del historial de puntos de un usuario. */
data class PointsEntry(
    val userId: String,
    val points: Int,
    val reason: String,
    val createdAt: LocalDateTime
)
