package co.edu.uniquindio.akjevents.domain.model

enum class EventStatus(val label: String) {
    PENDING("En revisión"),
    VERIFIED("Verificado"),
    REJECTED("Rechazado"),
    FINISHED("Finalizado")
}
