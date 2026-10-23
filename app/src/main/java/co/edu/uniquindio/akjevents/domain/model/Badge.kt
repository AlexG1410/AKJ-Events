package co.edu.uniquindio.akjevents.domain.model

/** Insignias que se desbloquean con logros dentro de la comunidad. */
enum class Badge(val label: String, val description: String) {
    FIRST_EVENT("Primer evento", "Publicaste tu primer evento comunitario"),
    FIRST_COMMENT("Primer aporte", "Comentaste por primera vez en un evento"),
    FIRST_CHECK_IN("Primera asistencia", "Registraste tu primera asistencia con QR"),
    TEN_VERIFIED("10 Verificados", "Diez de tus eventos fueron verificados"),
    FEATURED_OF_MONTH("Destacado del mes", "Tu evento fue el más destacado de su mes")
}
