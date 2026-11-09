package co.edu.uniquindio.akjevents.domain.model

/** Los 4 niveles de la temática, según los puntos de reputación acumulados. */
enum class UserLevel(val minPoints: Int) {
    SPECTATOR(0),
    PARTICIPANT(100),
    ORGANIZER(250),
    COMMUNITY_LEADER(500);

    /** Nivel 1 a 4. */
    val number: Int get() = ordinal + 1

    /** Siguiente nivel; null en el máximo. */
    val next: UserLevel? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): UserLevel = entries.last { points >= it.minPoints }
    }
}
