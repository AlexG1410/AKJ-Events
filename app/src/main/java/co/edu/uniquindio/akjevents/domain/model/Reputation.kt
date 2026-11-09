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

/** Por qué se sumaron o restaron puntos; la pantalla arma la frase en el idioma del usuario. */
enum class PointsReason {
    /** Puntos con los que arrancan las cuentas de demostración. */
    ACCUMULATED,
    CREATE_EVENT,
    EVENT_VERIFIED,
    EVENT_FINISHED,
    COMMENT,
    INTEREST_RECEIVED,
    INTEREST_REMOVED,
    CHECK_IN
}

/** Movimiento del historial de puntos de un usuario; [eventTitle] es el evento relacionado, si lo hay. */
data class PointsEntry(
    val userId: String,
    val points: Int,
    val reason: PointsReason,
    val createdAt: LocalDateTime,
    val eventTitle: String? = null
)
