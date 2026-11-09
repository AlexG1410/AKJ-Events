package co.edu.uniquindio.akjevents.core.util

import androidx.annotation.StringRes
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationGroup
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.UserLevel

/*
 * Nombres visibles de los enums del dominio. El dominio no conoce los recursos de Android:
 * guarda solo el valor, y aquí se elige el texto de strings.xml según el idioma del dispositivo.
 */

@get:StringRes
val EventCategory.labelRes: Int
    get() = when (this) {
        EventCategory.SPORTS -> R.string.category_sports
        EventCategory.CULTURE -> R.string.category_culture
        EventCategory.ACADEMIC -> R.string.category_academic
        EventCategory.VOLUNTEERING -> R.string.category_volunteering
        EventCategory.SOCIAL -> R.string.category_social
    }

@get:StringRes
val EventStatus.labelRes: Int
    get() = when (this) {
        EventStatus.PENDING -> R.string.status_pending
        EventStatus.VERIFIED -> R.string.status_verified
        EventStatus.REJECTED -> R.string.status_rejected
        EventStatus.FINISHED -> R.string.status_finished
    }

@get:StringRes
val UserLevel.labelRes: Int
    get() = when (this) {
        UserLevel.SPECTATOR -> R.string.level_spectator
        UserLevel.PARTICIPANT -> R.string.level_participant
        UserLevel.ORGANIZER -> R.string.level_organizer
        UserLevel.COMMUNITY_LEADER -> R.string.level_community_leader
    }

@get:StringRes
val Badge.labelRes: Int
    get() = when (this) {
        Badge.FIRST_EVENT -> R.string.badge_first_event
        Badge.FIRST_COMMENT -> R.string.badge_first_comment
        Badge.FIRST_CHECK_IN -> R.string.badge_first_check_in
        Badge.TEN_VERIFIED -> R.string.badge_ten_verified
        Badge.FEATURED_OF_MONTH -> R.string.badge_featured_of_month
    }

@get:StringRes
val Badge.descriptionRes: Int
    get() = when (this) {
        Badge.FIRST_EVENT -> R.string.badge_first_event_description
        Badge.FIRST_COMMENT -> R.string.badge_first_comment_description
        Badge.FIRST_CHECK_IN -> R.string.badge_first_check_in_description
        Badge.TEN_VERIFIED -> R.string.badge_ten_verified_description
        Badge.FEATURED_OF_MONTH -> R.string.badge_featured_of_month_description
    }

@get:StringRes
val NotificationGroup.labelRes: Int
    get() = when (this) {
        NotificationGroup.EVENTS -> R.string.notification_group_events
        NotificationGroup.MODERATION -> R.string.notification_group_moderation
        NotificationGroup.ACHIEVEMENTS -> R.string.notification_group_achievements
    }

/** Texto del movimiento de puntos; los que tienen evento llevan su título como argumento. */
@get:StringRes
val PointsEntry.reasonRes: Int
    get() = when (reason) {
        PointsReason.ACCUMULATED -> R.string.points_reason_accumulated
        PointsReason.CREATE_EVENT -> R.string.points_reason_create_event
        PointsReason.EVENT_VERIFIED -> R.string.points_reason_event_verified
        PointsReason.EVENT_FINISHED -> R.string.points_reason_event_finished
        PointsReason.COMMENT -> R.string.points_reason_comment
        PointsReason.INTEREST_RECEIVED -> R.string.points_reason_interest_received
        PointsReason.INTEREST_REMOVED -> R.string.points_reason_interest_removed
        PointsReason.CHECK_IN -> R.string.points_reason_check_in
    }
