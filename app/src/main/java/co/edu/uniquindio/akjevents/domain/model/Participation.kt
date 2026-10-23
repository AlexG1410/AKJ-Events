package co.edu.uniquindio.akjevents.domain.model

/** Asistencia confirmada por un usuario; [checkedIn] indica que ya validó su QR en el evento. */
data class Attendance(
    val eventId: String,
    val userId: String,
    val checkedIn: Boolean = false
)

/** Voto de "Me interesa" de un usuario en un evento. */
data class Interest(
    val eventId: String,
    val userId: String
)
