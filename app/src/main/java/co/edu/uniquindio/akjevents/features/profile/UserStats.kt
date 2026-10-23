package co.edu.uniquindio.akjevents.features.profile

import co.edu.uniquindio.akjevents.domain.model.Attendance
import co.edu.uniquindio.akjevents.domain.model.Comment
import co.edu.uniquindio.akjevents.domain.model.CommunityEvent
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import kotlin.math.roundToInt

/** Métricas personales del perfil (mockup 12) y del dashboard de estadísticas (mockup 15). */
data class UserStats(
    val created: Int = 0,
    val active: Int = 0,
    val finished: Int = 0,
    val pending: Int = 0,
    val rejected: Int = 0,
    val confirmedAttendances: Int = 0,
    val checkIns: Int = 0,
    val comments: Int = 0,
    /** Personas interesadas en los eventos del usuario. */
    val interestedNeighbors: Int = 0,
    /** Asistentes confirmados en los eventos del usuario. */
    val reachedAttendees: Int = 0,
    val eventsByCategory: Map<EventCategory, Int> = emptyMap()
) {
    /** Porcentaje de asistencias confirmadas que terminaron en check-in; null sin asistencias. */
    val complianceRate: Int?
        get() = if (confirmedAttendances == 0) null else (checkIns * 100f / confirmedAttendances).roundToInt()

    companion object {
        fun of(userId: String, events: List<CommunityEvent>, attendances: List<Attendance>, comments: List<Comment>): UserStats {
            val own = events.filter { it.organizerId == userId }
            val mine = attendances.filter { it.userId == userId }
            return UserStats(
                created = own.size,
                active = own.count { it.status == EventStatus.VERIFIED },
                finished = own.count { it.status == EventStatus.FINISHED },
                pending = own.count { it.status == EventStatus.PENDING },
                rejected = own.count { it.status == EventStatus.REJECTED },
                confirmedAttendances = mine.size,
                checkIns = mine.count { it.checkedIn },
                comments = comments.count { it.authorId == userId },
                interestedNeighbors = own.sumOf { it.interestCount },
                reachedAttendees = own.sumOf { it.attendanceCount },
                eventsByCategory = own.groupingBy { it.category }.eachCount()
            )
        }
    }
}
