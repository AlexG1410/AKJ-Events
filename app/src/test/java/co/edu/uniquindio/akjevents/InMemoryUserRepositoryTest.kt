package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.TestRepositories.Companion.MODERATOR
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.NotificationType
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryUserRepositoryTest {
    private val repos = TestRepositories(sessionUserId = null)
    private val users = repos.users

    @Test
    fun demoCredentialsOpenTheSession() {
        assertNull(users.login(DemoCredentials.email, "ClaveIncorrecta1"))
        assertNull(users.currentUser.value)

        val user = users.login(" ${DemoCredentials.email.uppercase()} ", DemoCredentials.password)

        assertEquals(DEMO_USER, user?.id)
        assertEquals(DEMO_USER, users.currentUser.value?.id)

        users.logout()
        assertNull(users.currentUser.value)
    }

    @Test
    fun preloadedModeratorHasTheModeratorRole() {
        val moderator = users.login(DemoCredentials.moderatorEmail, DemoCredentials.moderatorPassword)

        assertEquals(UserRole.MODERATOR, moderator?.role)
        assertTrue(moderator!!.isModerator)
    }

    @Test
    fun registrationCreatesASpectatorAndRejectsTakenEmails() {
        val location = Location(4.5296, -75.6431)
        val result = users.register("Ana Gómez", "ana@ejemplo.com", "Calarcá, Quindío", "Comunidad1", setOf(EventCategory.CULTURE), "Parque Bolívar", location)

        val user = (result as RegisterResult.Success).user
        assertEquals(UserRole.USER, user.role)
        assertEquals(UserLevel.SPECTATOR, user.level)
        assertEquals(user, users.currentUser.value)
        assertEquals("Parque Bolívar", user.address)
        assertEquals(location, user.location)
        assertNotNull(users.login("ana@ejemplo.com", "Comunidad1"))

        assertEquals(RegisterResult.EmailInUse, users.register("Otra", DemoCredentials.email.uppercase(), "Armenia, Quindío", "Comunidad1", emptySet(), "Parque Bolívar", location))
    }

    @Test
    fun levelIsDerivedFromPointsAndLevelUpIsNotified() {
        assertEquals(UserLevel.ORGANIZER, users.findById(DEMO_USER)!!.level)

        users.addPoints(DEMO_USER, 20, "Prueba")

        assertEquals(UserLevel.COMMUNITY_LEADER, users.findById(DEMO_USER)!!.level)
        assertTrue(repos.notificationsOf(DEMO_USER).any { it.type == NotificationType.LEVEL_UP })
        assertEquals("Prueba", users.pointsHistory.value.first().reason)
    }

    @Test
    fun levelThresholds() {
        assertEquals(UserLevel.SPECTATOR, UserLevel.forPoints(99))
        assertEquals(UserLevel.PARTICIPANT, UserLevel.forPoints(100))
        assertEquals(UserLevel.ORGANIZER, UserLevel.forPoints(250))
        assertEquals(UserLevel.COMMUNITY_LEADER, UserLevel.forPoints(500))
    }

    @Test
    fun badgesAreAwardedOnlyOnce() {
        users.awardBadge(DEMO_USER, Badge.TEN_VERIFIED)
        users.awardBadge(DEMO_USER, Badge.TEN_VERIFIED)

        assertTrue(Badge.TEN_VERIFIED in users.findById(DEMO_USER)!!.badges)
        assertEquals(1, repos.notificationsOf(DEMO_USER).count { it.type == NotificationType.BADGE_UNLOCKED })
    }

    @Test
    fun moderatorsDoNotEarnPoints() {
        users.addPoints(MODERATOR, 50, "Prueba")

        assertEquals(0, users.findById(MODERATOR)!!.points)
    }

    @Test
    fun profileUpdateKeepsEmailsUniqueAndMovesTheLogin() {
        val user = users.findById(DEMO_USER)!!

        assertEquals(ProfileUpdateResult.EMAIL_IN_USE, users.updateProfile(user.copy(email = DemoCredentials.moderatorEmail)))
        assertEquals(ProfileUpdateResult.UPDATED, users.updateProfile(user.copy(name = "Camilo R.", email = "camilo@nuevo.co", points = 9999)))

        val updated = users.findById(DEMO_USER)!!
        assertEquals("Camilo R.", updated.name)
        // Los puntos no se cambian desde el perfil
        assertEquals(user.points, updated.points)
        assertNotNull(users.login("camilo@nuevo.co", DemoCredentials.password))
        assertNull(users.login(DemoCredentials.email, DemoCredentials.password))
    }

    @Test
    fun deletingTheAccountClosesTheSession() {
        users.login(DemoCredentials.email, DemoCredentials.password)

        assertTrue(users.deleteAccount(DEMO_USER))

        assertNull(users.currentUser.value)
        assertNull(users.findById(DEMO_USER))
        assertNull(users.login(DemoCredentials.email, DemoCredentials.password))
    }
}
