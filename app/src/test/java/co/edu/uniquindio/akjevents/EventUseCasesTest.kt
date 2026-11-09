package co.edu.uniquindio.akjevents

import kotlinx.coroutines.test.runTest
import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.TestRepositories.Companion.MODERATOR
import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.usecase.participation.CheckInResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas de la comunidad: viven en los casos de uso, que usan el repositorio solo para leer y guardar.
 */
class EventUseCasesTest {
    private val repos = TestRepositories()
    private val repository = repos.events
    private val uc = repos.useCases

    private suspend fun attendance(id: String) = repository.findEvent(id)!!.attendanceCount

    private fun isAttending(eventId: String, userId: String = DEMO_USER) =
        repository.attendances.value.any { it.eventId == eventId && it.userId == userId }

    @Test
    fun onlyVerifiedEventsArePublic() = runTest {
        val events = repository.publicEvents.value

        assertEquals(8, events.size)
        assertTrue(events.all { it.status == EventStatus.VERIFIED })
        assertNull(repository.findPublicEvent("pendiente-privado"))
        assertNull(repository.findPublicEvent("rechazado-privado"))
        assertNull(repository.findPublicEvent("reforestacion-rio"))
    }

    @Test
    fun hiddenEventsCannotBeModified() = runTest {
        assertEquals(AttendanceResult.NOT_FOUND, uc.confirmAttendance("pendiente-privado", DEMO_USER))
        assertNull(uc.toggleInterest("rechazado-privado", DEMO_USER))
        assertEquals(AttendanceResult.CLOSED, uc.confirmAttendance("reforestacion-rio", DEMO_USER))
        assertFalse(isAttending("pendiente-privado"))
    }

    @Test
    fun confirmAddsOneAttendeeOnlyOnce() = runTest {
        val before = attendance("ciclopaseo")

        assertEquals(AttendanceResult.CONFIRMED, uc.confirmAttendance("ciclopaseo", DEMO_USER))
        assertEquals(AttendanceResult.ALREADY_CONFIRMED, uc.confirmAttendance("ciclopaseo", DEMO_USER))

        assertEquals(before + 1, attendance("ciclopaseo"))
        assertTrue(isAttending("ciclopaseo"))
    }

    @Test
    fun cancelRemovesTheAttendee() = runTest {
        val before = attendance("ciclopaseo")
        uc.confirmAttendance("ciclopaseo", DEMO_USER)

        assertTrue(uc.cancelAttendance("ciclopaseo", DEMO_USER))
        assertFalse(uc.cancelAttendance("ciclopaseo", DEMO_USER))

        assertEquals(before, attendance("ciclopaseo"))
        assertFalse(isAttending("ciclopaseo"))
    }

    @Test
    fun fullEventRejectsConfirmation() = runTest {
        val before = attendance("encuentro-vecinal")

        assertEquals(AttendanceResult.FULL, uc.confirmAttendance("encuentro-vecinal", DEMO_USER))

        assertEquals(before, attendance("encuentro-vecinal"))
        assertFalse(isAttending("encuentro-vecinal"))
    }

    @Test
    fun interestTogglesTheCounterAndRewardsTheOrganizer() = runTest {
        val before = repository.findPublicEvent("feria-cultural")!!.interestCount
        val organizerPoints = repos.pointsOf("organizer-2")

        assertEquals(true, uc.toggleInterest("feria-cultural", DEMO_USER))
        assertEquals(before + 1, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertEquals(organizerPoints + ReputationPoints.INTEREST_RECEIVED, repos.pointsOf("organizer-2"))

        assertEquals(false, uc.toggleInterest("feria-cultural", DEMO_USER))
        assertEquals(before, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertEquals(organizerPoints, repos.pointsOf("organizer-2"))
    }

    @Test
    fun createdPendingEventIsStoredButNotPublicAndGivesPoints() = runTest {
        val points = repos.pointsOf("organizer-1")
        val pending = SampleEvents.all.first().copy(id = "nuevo", organizerId = "organizer-1", status = EventStatus.PENDING)

        uc.createEvent(pending)

        assertTrue(pending in repository.findEventsByOrganizer("organizer-1"))
        assertNull(repository.findPublicEvent("nuevo"))
        assertEquals(8, repository.publicEvents.value.size)
        assertEquals(AttendanceResult.NOT_FOUND, uc.confirmAttendance("nuevo", DEMO_USER))
        assertEquals(points + ReputationPoints.CREATE_EVENT, repos.pointsOf("organizer-1"))
    }

    @Test
    fun moderatorVerificationPublishesNotifiesAndRewards() = runTest {
        val points = repos.pointsOf(DEMO_USER)

        assertTrue(uc.verifyEvent("huertos-urbanos", MODERATOR))

        assertNotNull(repository.findPublicEvent("huertos-urbanos"))
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.EVENT_VERIFIED && it.eventId == "huertos-urbanos" })
        assertTrue(repos.pointsOf(DEMO_USER) >= points + ReputationPoints.EVENT_VERIFIED)
    }

    @Test
    fun onlyModeratorsCanVerifyOrReject() = runTest {
        assertFalse(uc.verifyEvent("huertos-urbanos", DEMO_USER))
        assertFalse(uc.rejectEvent("huertos-urbanos", DEMO_USER, "Motivo"))
        assertEquals(EventStatus.PENDING, repository.findEvent("huertos-urbanos")!!.status)
    }

    @Test
    fun repeatedVerificationAfterEditingDoesNotRepeatPointsButStillNotifies() = runTest {
        val before = repos.pointsOf(DEMO_USER)
        assertTrue(uc.verifyEvent("huertos-urbanos", MODERATOR))
        assertEquals(before + ReputationPoints.EVENT_VERIFIED, repos.pointsOf(DEMO_USER))

        repeat(2) {
            val event = repository.findEvent("huertos-urbanos")!!
            assertTrue(uc.updateEvent(event.copy(title = "Huertos corregidos $it"), DEMO_USER))
            assertTrue(uc.verifyEvent(event.id, MODERATOR))
        }

        assertEquals(before + ReputationPoints.EVENT_VERIFIED, repos.pointsOf(DEMO_USER))
        assertEquals(3, repos.notificationsOf(DEMO_USER).count {
            it.type == NotificationType.EVENT_VERIFIED && it.eventId == "huertos-urbanos"
        })
        assertNotNull(repository.findPublicEvent("huertos-urbanos"))
    }

    @Test
    fun initiallyVerifiedEventsDoNotReceiveAnotherVerificationReward() = runTest {
        val event = repository.findEvent("cine-foro")!!
        val before = repos.pointsOf(DEMO_USER)

        assertTrue(uc.updateEvent(event, DEMO_USER))
        assertFalse(uc.verifyEvent(event.id, DEMO_USER))
        assertTrue(uc.verifyEvent(event.id, MODERATOR))

        assertEquals(before, repos.pointsOf(DEMO_USER))
    }

    @Test
    fun editingCannotReduceCapacityBelowCurrentAttendanceEvenWithAlteredCounter() = runTest {
        val event = repository.findEvent("cine-foro")!!
        assertTrue(event.attendanceCount > 0)

        assertFalse(uc.updateEvent(event.copy(capacity = event.attendanceCount - 1, attendanceCount = 0), DEMO_USER))
        assertEquals(event, repository.findEvent(event.id))

        assertTrue(uc.updateEvent(event.copy(capacity = event.attendanceCount), DEMO_USER))
        assertEquals(event.attendanceCount, repository.findEvent(event.id)!!.capacity)
        assertTrue(uc.updateEvent(event.copy(capacity = event.attendanceCount + 1), DEMO_USER))
        assertTrue(uc.updateEvent(event.copy(capacity = null), DEMO_USER))
        assertNull(repository.findEvent(event.id)!!.capacity)
    }

    @Test
    fun rejectionRequiresAReasonAndNotifiesTheOrganizer() = runTest {
        assertFalse(uc.rejectEvent("huertos-urbanos", MODERATOR, "   "))

        assertTrue(uc.rejectEvent("huertos-urbanos", MODERATOR, "Información insuficiente"))

        val event = repository.findEvent("huertos-urbanos")!!
        assertEquals(EventStatus.REJECTED, event.status)
        assertEquals("Información insuficiente", event.rejectionReason)
        assertNull(repository.findPublicEvent("huertos-urbanos"))
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.EVENT_REJECTED && it.message.contains("Información insuficiente") })
    }

    @Test
    fun commentNotifiesTheEventAuthor() = runTest {
        val commenterPoints = repos.pointsOf("organizer-4")

        val comment = uc.addComment("cine-foro", "organizer-4", "¿Habrá crispetas?")

        assertNotNull(comment)
        assertTrue(comment in repository.comments.value)
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.NEW_COMMENT && it.eventId == "cine-foro" })
        assertEquals(commenterPoints + ReputationPoints.COMMENT, repos.pointsOf("organizer-4"))
    }

    @Test
    fun ownCommentDoesNotNotifyAndHiddenEventsCannotBeCommented() = runTest {
        uc.addComment("cine-foro", DEMO_USER, "Los esperamos")
        assertTrue(repos.notificationsOf(DEMO_USER).none { it.type == NotificationType.NEW_COMMENT })

        assertNull(uc.addComment("pendiente-privado", DEMO_USER, "Hola"))
        assertNull(uc.addComment("cine-foro", DEMO_USER, "   "))
        // Un evento finalizado sigue aceptando comentarios
        assertNotNull(uc.addComment("reforestacion-rio", "organizer-4", "¡Gran jornada!"))
    }

    @Test
    fun onlyTheOrganizerOrAModeratorCanFinishAnEvent() = runTest {
        assertFalse(uc.finishEvent("cine-foro", "organizer-4"))

        assertTrue(uc.finishEvent("cine-foro", DEMO_USER))

        assertEquals(EventStatus.FINISHED, repository.findEvent("cine-foro")!!.status)
        assertNull(repository.findPublicEvent("cine-foro"))
        // Los asistentes con cuenta reciben el aviso
        assertTrue(repos.notificationsOf("organizer-4").any { it.type == NotificationType.EVENT_FINISHED })

        assertTrue(uc.finishEvent("ciclopaseo", MODERATOR))
        assertTrue(repos.notificationsOf("organizer-1").any { it.type == NotificationType.EVENT_FINISHED })
    }

    @Test
    fun checkInWithTheEventCodeRegistersAttendanceAndPoints() = runTest {
        val code = repository.findEvent("ciclopaseo")!!.checkInCode
        uc.confirmAttendance("ciclopaseo", DEMO_USER)
        val points = repos.pointsOf(DEMO_USER)

        val result = uc.checkIn(code.lowercase(), DEMO_USER)

        assertTrue(result is CheckInResult.Success)
        assertTrue(repository.attendances.value.single { it.eventId == "ciclopaseo" && it.userId == DEMO_USER }.checkedIn)
        assertEquals(points + ReputationPoints.CHECK_IN, repos.pointsOf(DEMO_USER))
        assertTrue(Badge.FIRST_CHECK_IN in repos.users.findById(DEMO_USER)!!.badges)
        assertTrue(uc.checkIn(code, DEMO_USER) is CheckInResult.AlreadyCheckedIn)
        // Tras el check-in ya no se puede cancelar la asistencia
        assertFalse(uc.cancelAttendance("ciclopaseo", DEMO_USER))
    }

    @Test
    fun checkInRejectsInvalidCodesAndOwnEvents() = runTest {
        assertEquals(CheckInResult.InvalidCode, uc.checkIn("AKJ-NOEXISTE", DEMO_USER))
        assertTrue(uc.checkIn(repository.findEvent("cine-foro")!!.checkInCode, DEMO_USER) is CheckInResult.OwnEvent)
        // El código de un evento pendiente no sirve
        assertEquals(CheckInResult.InvalidCode, uc.checkIn(repository.findEvent("huertos-urbanos")!!.checkInCode, DEMO_USER))
    }

    @Test
    fun checkInWithoutConfirmingTakesASpotIfAvailable() = runTest {
        val before = attendance("taller-kotlin")

        assertTrue(uc.checkIn(repository.findEvent("taller-kotlin")!!.checkInCode, DEMO_USER) is CheckInResult.Success)
        assertEquals(before + 1, attendance("taller-kotlin"))

        assertTrue(uc.checkIn(repository.findEvent("encuentro-vecinal")!!.checkInCode, DEMO_USER) is CheckInResult.Full)
    }

    @Test
    fun editingAnEventSendsItBackToReview() = runTest {
        val event = repository.findEvent("cine-foro")!!

        assertFalse(uc.updateEvent(event.copy(title = "Otro título"), "organizer-4"))
        assertTrue(uc.updateEvent(event.copy(title = "Cine foro bajo las estrellas", attendanceCount = 0), DEMO_USER))

        val updated = repository.findEvent("cine-foro")!!
        assertEquals("Cine foro bajo las estrellas", updated.title)
        assertEquals(EventStatus.PENDING, updated.status)
        // Los conteos no se pueden alterar al editar
        assertEquals(event.attendanceCount, updated.attendanceCount)
        assertNull(repository.findPublicEvent("cine-foro"))
        assertFalse(uc.updateEvent(repository.findEvent("reforestacion-rio")!!, DEMO_USER))
    }

    @Test
    fun deletingAnEventRemovesItsCommentsAndOnlyTheOwnerCanDoIt() = runTest {
        assertFalse(uc.deleteEvent("ciclopaseo", DEMO_USER))

        assertTrue(uc.deleteEvent("cine-foro", DEMO_USER))

        assertNull(repository.findEvent("cine-foro"))
        assertTrue(repository.comments.value.none { it.eventId == "cine-foro" })
        assertTrue(repository.attendances.value.none { it.eventId == "cine-foro" })
    }

    @Test
    fun removingUserDataCleansTheirEventsAndParticipation() = runTest {
        val feriaBefore = attendance("feria-cultural")

        repository.removeUserData(DEMO_USER)

        assertTrue(repository.findEventsByOrganizer(DEMO_USER).isEmpty())
        assertTrue(repository.comments.value.none { it.authorId == DEMO_USER })
        assertFalse(isAttending("feria-cultural"))
        assertEquals(feriaBefore - 1, attendance("feria-cultural"))
    }

    @Test
    fun checkInCodesAreUniqueAmongSampleEvents() = runTest {
        val codes = SampleEvents.all.map { it.checkInCode }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun theRepositoryAloneOnlyStoresData() = runTest {
        val points = repos.pointsOf("organizer-1")
        val event = SampleEvents.all.first().copy(id = "directo", organizerId = "organizer-1", status = EventStatus.VERIFIED)

        // Guardar directamente en el repositorio no aplica reglas: no da puntos ni cambia el estado
        repository.createEvent(event)
        repository.addComment("ciclopaseo", "organizer-4", "Sin notificación")

        assertEquals(points, repos.pointsOf("organizer-1"))
        assertEquals(EventStatus.VERIFIED, repository.findEvent("directo")!!.status)
        assertTrue(repos.notifications.notifications.value.isEmpty())
    }

    @Test
    fun theUseCaseAlwaysCreatesEventsPendingOfReview() = runTest {
        uc.createEvent(SampleEvents.all.first().copy(id = "nuevo-verificado", organizerId = "organizer-1", status = EventStatus.VERIFIED))

        assertEquals(EventStatus.PENDING, repository.findEvent("nuevo-verificado")!!.status)
        assertNull(repository.findPublicEvent("nuevo-verificado"))
    }
}
