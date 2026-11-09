package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.InMemoryNotificationRepository
import co.edu.uniquindio.akjevents.data.repository.InMemorySessionPreferences
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.NotificationRepository
import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import co.edu.uniquindio.akjevents.domain.usecase.auth.LoginUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.LogoutUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.ObserveCurrentUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.RegisterUserUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.RestoreSessionUseCase
import co.edu.uniquindio.akjevents.domain.usecase.auth.SendPasswordResetUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.CreateEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.DeleteEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.FinishEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.GetEventByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.GetPublicEventByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObserveEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.ObservePublicEventsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.SendNoticeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.event.UpdateEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.moderation.RejectEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.moderation.VerifyEventUseCase
import co.edu.uniquindio.akjevents.domain.usecase.notification.MarkAllNotificationsReadUseCase
import co.edu.uniquindio.akjevents.domain.usecase.notification.ObserveNotificationsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.notification.SetNotificationReadUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.AddCommentUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.CancelAttendanceUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ConfirmAttendanceUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveAttendancesUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveCommentsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ObserveInterestsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.participation.ToggleInterestUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardBadgeUseCase
import co.edu.uniquindio.akjevents.domain.usecase.reputation.AwardPointsUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.DeleteAccountUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.GetUserByIdUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.ObservePointsHistoryUseCase
import co.edu.uniquindio.akjevents.domain.usecase.user.UpdateProfileUseCase

/** Los casos de uso conectados a unos repositorios de prueba, como los arma Hilt en la app. */
class TestUseCases(
    events: EventRepository,
    users: UserRepository,
    notifications: NotificationRepository = InMemoryNotificationRepository(emptyList()),
    val sessionPreferences: SessionPreferencesRepository = InMemorySessionPreferences()
) {
    // Sesión y usuarios
    val observeCurrentUser = ObserveCurrentUserUseCase(users)
    val login = LoginUseCase(users, sessionPreferences)
    val logout = LogoutUseCase(users)
    val registerUser = RegisterUserUseCase(users, sessionPreferences)
    val sendPasswordReset = SendPasswordResetUseCase(users)
    val restoreSession = RestoreSessionUseCase(users, sessionPreferences)
    val getUserById = GetUserByIdUseCase(users)
    val updateProfile = UpdateProfileUseCase(users)
    val deleteAccount = DeleteAccountUseCase(users, events, notifications)
    val observePointsHistory = ObservePointsHistoryUseCase(users)

    // Reputación
    val awardPoints = AwardPointsUseCase(users, notifications)
    val awardBadge = AwardBadgeUseCase(users, notifications)

    // Eventos y moderación
    val observePublicEvents = ObservePublicEventsUseCase(events)
    val observeEvents = ObserveEventsUseCase(events)
    val getEventById = GetEventByIdUseCase(events)
    val getPublicEventById = GetPublicEventByIdUseCase(events)
    val createEvent = CreateEventUseCase(events, awardPoints, awardBadge)
    val updateEvent = UpdateEventUseCase(events)
    val deleteEvent = DeleteEventUseCase(events, users)
    val finishEvent = FinishEventUseCase(events, users, notifications, awardPoints)
    val sendNotice = SendNoticeUseCase(events, notifications)
    val verifyEvent = VerifyEventUseCase(events, users, notifications, awardPoints, awardBadge)
    val rejectEvent = RejectEventUseCase(events, users, notifications)

    // Participación
    val observeAttendances = ObserveAttendancesUseCase(events)
    val observeInterests = ObserveInterestsUseCase(events)
    val observeComments = ObserveCommentsUseCase(events)
    val confirmAttendance = ConfirmAttendanceUseCase(events)
    val cancelAttendance = CancelAttendanceUseCase(events)
    val checkIn = CheckInUseCase(events, awardPoints, awardBadge)
    val toggleInterest = ToggleInterestUseCase(events, awardPoints, awardBadge)
    val addComment = AddCommentUseCase(events, users, notifications, awardPoints, awardBadge)

    // Notificaciones
    val observeNotifications = ObserveNotificationsUseCase(notifications)
    val setNotificationRead = SetNotificationReadUseCase(notifications)
    val markAllNotificationsRead = MarkAllNotificationsReadUseCase(notifications)
}
