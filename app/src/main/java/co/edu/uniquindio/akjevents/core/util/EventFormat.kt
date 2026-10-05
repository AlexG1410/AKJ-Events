package co.edu.uniquindio.akjevents.core.util

import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatos de fecha y cupos con el estilo de los mockups ("Jueves 24 Oct • 7:00 PM"). */
object EventFormat {
    private val spanish = Locale.forLanguageTag("es-CO")
    private val weekday = DateTimeFormatter.ofPattern("EEEE", spanish)
    private val month = DateTimeFormatter.ofPattern("MMM", spanish)
    private val monthLong = DateTimeFormatter.ofPattern("MMMM", spanish)
    // Locale.US para que la hora salga como "7:00 PM" y no "7:00 p. m."
    private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    /** "OCT" */
    fun monthBadge(date: LocalDateTime): String = shortMonth(date).uppercase(spanish)

    /** "Jueves 24 Oct • 7:00 PM" */
    fun weekdayDateTime(date: LocalDateTime): String =
        "${weekday.format(date).capitalized()} ${date.dayOfMonth} ${shortMonth(date).capitalized()} • ${time.format(date)}"

    /** "Jueves, 24 de octubre de 2024" */
    fun longDate(date: LocalDateTime): String =
        "${weekday.format(date).capitalized()}, ${date.dayOfMonth} de ${monthLong.format(date)} de ${date.year}"

    /** "7:00 PM - 10:00 PM (3 horas)" */
    fun timeRange(start: LocalDateTime, end: LocalDateTime): String =
        "${time.format(start)} - ${time.format(end)} (${duration(start, end)})"

    private fun duration(start: LocalDateTime, end: LocalDateTime): String {
        val minutes = Duration.between(start, end).toMinutes()
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours == 0L -> "$rest min"
            rest == 0L -> if (hours == 1L) "1 hora" else "$hours horas"
            else -> "$hours h $rest min"
        }
    }

    // "oct." -> "oct"
    private fun shortMonth(date: LocalDateTime): String = month.format(date).removeSuffix(".")

    private fun String.capitalized(): String = replaceFirstChar { it.titlecase(spanish) }
}

/** Cupos libres; null cuando el evento no tiene límite. */
val CommunityEvent.remainingSpots: Int?
    get() = capacity?.let { (it - attendanceCount).coerceAtLeast(0) }

/** Porcentaje del cupo ocupado (0..1); null cuando el evento no tiene límite. */
val CommunityEvent.occupancy: Float?
    get() = capacity?.let { (attendanceCount.toFloat() / it).coerceIn(0f, 1f) }

val CommunityEvent.isFull: Boolean
    get() = remainingSpots == 0
