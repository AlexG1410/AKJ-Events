package co.edu.uniquindio.akjevents.domain.model

/** Los 4 niveles de la temática, según los puntos de reputación acumulados. */
enum class UserLevel(val label: String, val minPoints: Int) {
    SPECTATOR("Espectador", 0),
    PARTICIPANT("Participante", 100),
    ORGANIZER("Organizador", 250),
    COMMUNITY_LEADER("Líder Comunitario", 500);

    /** Nivel 1 a 4. */
    val number: Int get() = ordinal + 1

    /** Siguiente nivel; null en el máximo. */
    val next: UserLevel? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forPoints(points: Int): UserLevel = entries.last { points >= it.minPoints }
    }
}
