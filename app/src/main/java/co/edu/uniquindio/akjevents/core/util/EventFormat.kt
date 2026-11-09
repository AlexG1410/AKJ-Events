package co.edu.uniquindio.akjevents.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatos de fecha y cupos con el estilo de los mockups ("Jueves 24 Oct • 7:00 PM").
 * Los nombres de días y meses salen del idioma del dispositivo, y el orden de las partes de strings.xml.
 */
object EventFormat {
    // Locale.US para que la hora salga como "7:00 PM" y no "7:00 p. m."
    private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    @Composable
    private fun locale(): Locale = LocalConfiguration.current.locales[0]

    /** "OCT" */
    @Composable
    fun monthBadge(date: LocalDateTime): String = shortMonth(date, locale()).uppercase(locale())

    /** "Jueves 24 Oct • 7:00 PM" */
    @Composable
    fun weekdayDateTime(date: LocalDateTime): String {
        val locale = locale()
        return stringResource(
            R.string.date_weekday_datetime,
            weekday(date, locale), date.dayOfMonth, shortMonth(date, locale).capitalized(locale), time.format(date)
        )
    }

    /** "Jueves, 24 de octubre de 2024" */
    @Composable
    fun longDate(date: LocalDateTime): String {
        val locale = locale()
        return stringResource(
            R.string.date_long,
            weekday(date, locale), date.dayOfMonth, DateTimeFormatter.ofPattern("MMMM", locale).format(date), date.year
        )
    }

    /** "Sábado 02 Nov 2024" */
    @Composable
    fun weekdayFullDate(date: LocalDateTime): String {
        val locale = locale()
        return stringResource(
            R.string.date_weekday_full,
            weekday(date, locale), date.dayOfMonth, shortMonth(date, locale).capitalized(locale), date.year
        )
    }

    /** "9:00 AM" */
    fun timeOfDay(date: LocalDateTime): String = time.format(date)

    /** "12:30 PM (3 h 30 min)" */
    @Composable
    fun timeWithDuration(start: LocalDateTime, end: LocalDateTime): String =
        stringResource(R.string.time_with_duration, time.format(end), duration(start, end))

    /** "7:00 PM - 10:00 PM (3 horas)" */
    @Composable
    fun timeRange(start: LocalDateTime, end: LocalDateTime): String =
        stringResource(R.string.time_range, time.format(start), time.format(end), duration(start, end))

    /** "Sáb 02 Nov • 9:00 AM" */
    @Composable
    fun shortDateTime(date: LocalDateTime): String {
        val locale = locale()
        return stringResource(
            R.string.date_short_datetime,
            weekday(date, locale).take(3), date.dayOfMonth, shortMonth(date, locale).capitalized(locale), time.format(date)
        )
    }

    /** "Hace 25 min", "Hace 3 horas", "Ayer", "Hace 4 días" o la fecha si es más antigua. */
    @Composable
    fun relative(date: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(date, now).toMinutes()
        return when {
            minutes < 1 -> stringResource(R.string.relative_now)
            minutes < 60 -> pluralStringResource(R.plurals.relative_minutes_ago, minutes.toInt(), minutes.toInt())
            minutes < 24 * 60 -> (minutes / 60).toInt().let { pluralStringResource(R.plurals.relative_hours_ago, it, it) }
            minutes < 48 * 60 -> stringResource(R.string.relative_yesterday)
            minutes < 7 * 24 * 60 -> (minutes / (24 * 60)).toInt().let { pluralStringResource(R.plurals.relative_days_ago, it, it) }
            else -> stringResource(
                R.string.date_day_month_year, date.dayOfMonth, shortMonth(date, locale()).capitalized(locale()), date.year
            )
        }
    }

    /** "850 m" o "4.2 km" */
    fun distance(km: Double): String =
        if (km < 1) "${(km * 1000).toInt()} m" else "${"%.1f".format(Locale.US, km)} km"

    @Composable
    private fun duration(start: LocalDateTime, end: LocalDateTime): String {
        val minutes = Duration.between(start, end).toMinutes().toInt()
        val hours = minutes / 60
        val rest = minutes % 60
        return when {
            hours == 0 -> stringResource(R.string.duration_minutes, rest)
            rest == 0 -> pluralStringResource(R.plurals.duration_hours, hours, hours)
            else -> stringResource(R.string.duration_hours_minutes, hours, rest)
        }
    }

    private fun weekday(date: LocalDateTime, locale: Locale): String =
        DateTimeFormatter.ofPattern("EEEE", locale).format(date).capitalized(locale)

    // "oct." -> "oct"
    private fun shortMonth(date: LocalDateTime, locale: Locale): String =
        DateTimeFormatter.ofPattern("MMM", locale).format(date).removeSuffix(".")

    private fun String.capitalized(locale: Locale): String = replaceFirstChar { it.titlecase(locale) }
}

/** Cupos libres; null cuando el evento no tiene límite. */
val CommunityEvent.remainingSpots: Int?
    get() = capacity?.let { (it - attendanceCount).coerceAtLeast(0) }

/** Porcentaje del cupo ocupado (0..1); null cuando el evento no tiene límite. */
val CommunityEvent.occupancy: Float?
    get() = capacity?.let { (attendanceCount.toFloat() / it).coerceIn(0f, 1f) }

val CommunityEvent.isFull: Boolean
    get() = remainingSpots == 0
