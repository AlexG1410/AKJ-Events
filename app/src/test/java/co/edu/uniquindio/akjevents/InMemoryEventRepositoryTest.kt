package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.TestRepositories.Companion.MODERATOR
import co.edu.uniquindio.akjevents.data.demo.SampleEvents
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.ReputationPoints
import co.edu.uniquindio.akjevents.domain.repository.AttendanceResult
import co.edu.uniquindio.akjevents.domain.repository.CheckInResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryEventRepositoryTest {
    private val repos = TestRepositories()
    private val repository = repos.events

    private fun attendance(id: String) = repository.findEvent(id)!!.attendanceCount

    private fun isAttending(eventId: String, userId: String = DEMO_USER) =
        repository.attendances.value.any { it.eventId == eventId && it.userId == userId }

    @Test
    fun onlyVerifiedEventsArePublic() {
        val events = repository.publicEvents.value

        assertEquals(8, events.size)
        assertTrue(events.all { it.status == EventStatus.VERIFIED })
        assertNull(repository.findPublicEvent("pendiente-privado"))
        assertNull(repository.findPublicEvent("rechazado-privado"))
        assertNull(repository.findPublicEvent("reforestacion-rio"))
    }

    @Test
    fun hiddenEventsCannotBeModified() {
        assertEquals(AttendanceResult.NOT_FOUND, repository.confirmAttendance("pendiente-privado", DEMO_USER))
        assertNull(repository.toggleInterest("rechazado-privado", DEMO_USER))
        assertEquals(AttendanceResult.CLOSED, repository.confirmAttendance("reforestacion-rio", DEMO_USER))
        assertFalse(isAttending("pendiente-privado"))
    }

    @Test
    fun confirmAddsOneAttendeeOnlyOnce() {
        val before = attendance("ciclopaseo")

        assertEquals(AttendanceResult.CONFIRMED, repository.confirmAttendance("ciclopaseo", DEMO_USER))
        assertEquals(AttendanceResult.ALREADY_CONFIRMED, repository.confirmAttendance("ciclopaseo", DEMO_USER))

        assertEquals(before + 1, attendance("ciclopaseo"))
        assertTrue(isAttending("ciclopaseo"))
    }

    @Test
    fun cancelRemovesTheAttendee() {
        val before = attendance("ciclopaseo")
        repository.confirmAttendance("ciclopaseo", DEMO_USER)

        assertTrue(repository.cancelAttendance("ciclopaseo", DEMO_USER))
        assertFalse(repository.cancelAttendance("ciclopaseo", DEMO_USER))

        assertEquals(before, attendance("ciclopaseo"))
        assertFalse(isAttending("ciclopaseo"))
    }

    @Test
    fun fullEventRejectsConfirmation() {
        val before = attendance("encuentro-vecinal")

        assertEquals(AttendanceResult.FULL, repository.confirmAttendance("encuentro-vecinal", DEMO_USER))

        assertEquals(before, attendance("encuentro-vecinal"))
        assertFalse(isAttending("encuentro-vecinal"))
    }

    @Test
    fun interestTogglesTheCounterAndRewardsTheOrganizer() {
        val before = repository.findPublicEvent("feria-cultural")!!.interestCount
        val organizerPoints = repos.pointsOf("organizer-2")

        assertEquals(true, repository.toggleInterest("feria-cultural", DEMO_USER))
        assertEquals(before + 1, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertEquals(organizerPoints + ReputationPoints.INTEREST_RECEIVED, repos.pointsOf("organizer-2"))

        assertEquals(false, repository.toggleInterest("feria-cultural", DEMO_USER))
        assertEquals(before, repository.findPublicEvent("feria-cultural")!!.interestCount)
        assertEquals(organizerPoints, repos.pointsOf("organizer-2"))
    }

    @Test
    fun createdPendingEventIsStoredButNotPublicAndGivesPoints() {
        val points = repos.pointsOf("organizer-1")
        val pending = SampleEvents.all.first().copy(id = "nuevo", organizerId = "organizer-1", status = EventStatus.PENDING)

        repository.createEvent(pending)

        assertTrue(pending in repository.findEventsByOrganizer("organizer-1"))
        assertNull(repository.findPublicEvent("nuevo"))
        assertEquals(8, repository.publicEvents.value.size)
        assertEquals(AttendanceResult.NOT_FOUND, repository.confirmAttendance("nuevo", DEMO_USER))
        assertEquals(points + ReputationPoints.CREATE_EVENT, repos.pointsOf("organizer-1"))
    }

    @Test
    fun moderatorVerificationPublishesNotifiesAndRewards() {
        val points = repos.pointsOf(DEMO_USER)

        assertTrue(repository.verifyEvent("huertos-urbanos", MODERATOR))

        assertNotNull(repository.findPublicEvent("huertos-urbanos"))
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.EVENT_VERIFIED && it.eventId == "huertos-urbanos" })
        assertTrue(repos.pointsOf(DEMO_USER) >= points + ReputationPoints.EVENT_VERIFIED)
    }

    @Test
    fun onlyModeratorsCanVerifyOrReject() {
        assertFalse(repository.verifyEvent("huertos-urbanos", DEMO_USER))
        assertFalse(repository.rejectEvent("huertos-urbanos", DEMO_USER, "Motivo"))
        assertEquals(EventStatus.PENDING, repository.findEvent("huertos-urbanos")!!.status)
    }

    @Test
    fun repeatedVerificationAfterEditingDoesNotRepeatPointsButStillNotifies() {
        val before = repos.pointsOf(DEMO_USER)
        assertTrue(repository.verifyEvent("huertos-urbanos", MODERATOR))
        assertEquals(before + ReputationPoints.EVENT_VERIFIED, repos.pointsOf(DEMO_USER))

        repeat(2) {
            val event = repository.findEvent("huertos-urbanos")!!
            assertTrue(repository.updateEvent(event.copy(title = "Huertos corregidos $it"), DEMO_USER))
            assertTrue(repository.verifyEvent(event.id, MODERATOR))
        }

        assertEquals(before + ReputationPoints.EVENT_VERIFIED, repos.pointsOf(DEMO_USER))
        assertEquals(3, repos.notificationsOf(DEMO_USER).count {
            it.type == NotificationType.EVENT_VERIFIED && it.eventId == "huertos-urbanos"
        })
        assertNotNull(repository.findPublicEvent("huertos-urbanos"))
    }

    @Test
    fun initiallyVerifiedEventsDoNotReceiveAnotherVerificationReward() {
        val event = repository.findEvent("cine-foro")!!
        val before = repos.pointsOf(DEMO_USER)

        assertTrue(repository.updateEvent(event, DEMO_USER))
        assertFalse(repository.verifyEvent(event.id, DEMO_USER))
        assertTrue(repository.verifyEvent(event.id, MODERATOR))

        assertEquals(before, repos.pointsOf(DEMO_USER))
    }

    @Test
    fun editingCannotReduceCapacityBelowCurrentAttendanceEvenWithAlteredCounter() {
        val event = repository.findEvent("cine-foro")!!
        assertTrue(event.attendanceCount > 0)

        assertFalse(repository.updateEvent(event.copy(capacity = event.attendanceCount - 1, attendanceCount = 0), DEMO_USER))
        assertEquals(event, repository.findEvent(event.id))

        assertTrue(repository.updateEvent(event.copy(capacity = event.attendanceCount), DEMO_USER))
        assertEquals(event.attendanceCount, repository.findEvent(event.id)!!.capacity)
        assertTrue(repository.updateEvent(event.copy(capacity = event.attendanceCount + 1), DEMO_USER))
        assertTrue(repository.updateEvent(event.copy(capacity = null), DEMO_USER))
        assertNull(repository.findEvent(event.id)!!.capacity)
    }

    @Test
    fun rejectionRequiresAReasonAndNotifiesTheOrganizer() {
        assertFalse(repository.rejectEvent("huertos-urbanos", MODERATOR, "   "))

        assertTrue(repository.rejectEvent("huertos-urbanos", MODERATOR, "Información insuficiente"))

        val event = repository.findEvent("huertos-urbanos")!!
        assertEquals(EventStatus.REJECTED, event.status)
        assertEquals("Información insuficiente", event.rejectionReason)
        assertNull(repository.findPublicEvent("huertos-urbanos"))
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.EVENT_REJECTED && it.message.contains("Información insuficiente") })
    }

    @Test
    fun commentNotifiesTheEventAuthor() {
        val commenterPoints = repos.pointsOf("organizer-4")

        val comment = repository.addComment("cine-foro", "organizer-4", "¿Habrá crispetas?")

        assertNotNull(comment)
        assertTrue(comment in repository.comments.value)
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.NEW_COMMENT && it.eventId == "cine-foro" })
        assertEquals(commenterPoints + ReputationPoints.COMMENT, repos.pointsOf("organizer-4"))
    }

    @Test
    fun ownCommentDoesNotNotifyAndHiddenEventsCannotBeCommented() {
        repository.addComment("cine-foro", DEMO_USER, "Los esperamos")
        assertTrue(repos.notificationsOf(DEMO_USER).none { it.type == NotificationType.NEW_COMMENT })

        assertNull(repository.addComment("pendiente-privado", DEMO_USER, "Hola"))
        assertNull(repository.addComment("cine-foro", DEMO_USER, "   "))
        // Un evento finalizado sigue aceptando comentarios
        assertNotNull(repository.addComment("reforestacion-rio", "organizer-4", "¡Gran jornada!"))
    }

    @Test
    fun onlyTheOrganizerOrAModeratorCanFinishAnEvent() {
        assertFalse(repository.finishEvent("cine-foro", "organizer-4"))

        assertTrue(repository.finishEvent("cine-foro", DEMO_USER))

        assertEquals(EventStatus.FINISHED, repository.findEvent("cine-foro")!!.status)
        assertNull(repository.findPublicEvent("cine-foro"))
        // Los asistentes con cuenta reciben el aviso
        assertTrue(repos.notificationsOf("organizer-4").any { it.type == NotificationType.EVENT_FINISHED })

        assertTrue(repository.finishEvent("ciclopaseo", MODERATOR))
        assertTrue(repos.notificationsOf("organizer-1").any { it.type == NotificationType.EVENT_FINISHED })
    }

    @Test
    fun checkInWithTheEventCodeRegistersAttendanceAndPoints() {
        val code = repository.findEvent("ciclopaseo")!!.checkInCode
        repository.confirmAttendance("ciclopaseo", DEMO_USER)
        val points = repos.pointsOf(DEMO_USER)

        val result = repository.checkIn(code.lowercase(), DEMO_USER)

        assertTrue(result is CheckInResult.Success)
        assertTrue(repository.attendances.value.single { it.eventId == "ciclopaseo" && it.userId == DEMO_USER }.checkedIn)
        assertEquals(points + ReputationPoints.CHECK_IN, repos.pointsOf(DEMO_USER))
        assertTrue(Badge.FIRST_CHECK_IN in repos.users.findById(DEMO_USER)!!.badges)
        assertTrue(repository.checkIn(code, DEMO_USER) is CheckInResult.AlreadyCheckedIn)
        // Tras el check-in ya no se puede cancelar la asistencia
        assertFalse(repository.cancelAttendance("ciclopaseo", DEMO_USER))
    }

    @Test
    fun checkInRejectsInvalidCodesAndOwnEvents() {
        assertEquals(CheckInResult.InvalidCode, repository.checkIn("AKJ-NOEXISTE", DEMO_USER))
        assertTrue(repository.checkIn(repository.findEvent("cine-foro")!!.checkInCode, DEMO_USER) is CheckInResult.OwnEvent)
        // El código de un evento pendiente no sirve
        assertEquals(CheckInResult.InvalidCode, repository.checkIn(repository.findEvent("huertos-urbanos")!!.checkInCode, DEMO_USER))
    }

    @Test
    fun checkInWithoutConfirmingTakesASpotIfAvailable() {
        val before = attendance("taller-kotlin")

        assertTrue(repository.checkIn(repository.findEvent("taller-kotlin")!!.checkInCode, DEMO_USER) is CheckInResult.Success)
        assertEquals(before + 1, attendance("taller-kotlin"))

        assertTrue(repository.checkIn(repository.findEvent("encuentro-vecinal")!!.checkInCode, DEMO_USER) is CheckInResult.Full)
    }

    @Test
    fun editingAnEventSendsItBackToReview() {
        val event = repository.findEvent("cine-foro")!!

        assertFalse(repository.updateEvent(event.copy(title = "Otro título"), "organizer-4"))
        assertTrue(repository.updateEvent(event.copy(title = "Cine foro bajo las estrellas", attendanceCount = 0), DEMO_USER))

        val updated = repository.findEvent("cine-foro")!!
        assertEquals("Cine foro bajo las estrellas", updated.title)
        assertEquals(EventStatus.PENDING, updated.status)
        // Los conteos no se pueden alterar al editar
        assertEquals(event.attendanceCount, updated.attendanceCount)
        assertNull(repository.findPublicEvent("cine-foro"))
        assertFalse(repository.updateEvent(repository.findEvent("reforestacion-rio")!!, DEMO_USER))
    }

    @Test
    fun deletingAnEventRemovesItsCommentsAndOnlyTheOwnerCanDoIt() {
        assertFalse(repository.deleteEvent("ciclopaseo", DEMO_USER))

        assertTrue(repository.deleteEvent("cine-foro", DEMO_USER))

        assertNull(repository.findEvent("cine-foro"))
        assertTrue(repository.comments.value.none { it.eventId == "cine-foro" })
        assertTrue(repository.attendances.value.none { it.eventId == "cine-foro" })
    }

    @Test
    fun removingUserDataCleansTheirEventsAndParticipation() {
        val feriaBefore = attendance("feria-cultural")

        repository.removeUserData(DEMO_USER)

        assertTrue(repository.findEventsByOrganizer(DEMO_USER).isEmpty())
        assertTrue(repository.comments.value.none { it.authorId == DEMO_USER })
        assertFalse(isAttending("feria-cultural"))
        assertEquals(feriaBefore - 1, attendance("feria-cultural"))
    }

    @Test
    fun checkInCodesAreUniqueAmongSampleEvents() {
        val codes = SampleEvents.all.map { it.checkInCode }
        assertEquals(codes.size, codes.toSet().size)
    }
}
