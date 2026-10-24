package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.data.repository.LocalRecommendationRepository
import co.edu.uniquindio.akjevents.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LocalRecommendationRankerTest {
    @Test
    fun explicitInterestsAndCheckInCategoriesImproveAffinity() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("deportes"), recommendationEvent("cultura", category = EventCategory.CULTURE),
            recommendationEvent("historia", category = EventCategory.CULTURE, status = EventStatus.FINISHED)
        ), attendances = listOf(Attendance("historia", "lector", true)))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow })
        assertEquals("cultura", repository.recommend().items.first().event.id)

        fixture.users.updateProfile(fixture.user.copy(interests = setOf(EventCategory.SPORTS)))
        assertEquals("deportes", repository.recommend().items.first().event.id)
    }

    @Test
    fun historicalTitleAndDescriptionContributeBeyondCategory() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("a-sin-tema", category = EventCategory.SOCIAL),
            recommendationEvent("b-huertos", category = EventCategory.SOCIAL, description = "Huertos semillas balcones"),
            recommendationEvent("historia", category = EventCategory.VOLUNTEERING, title = "Huertos", description = "Semillas balcones", status = EventStatus.FINISHED)
        ), liked = listOf(Interest("historia", "lector")))
        val feed = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow }).recommend()
        assertEquals("b-huertos", feed.items.first().event.id)
        assertTrue(feed.items.first().reason.contains("temas"))
    }

    @Test
    fun coldStartUsesDistanceAvailabilityAndVarietyDeterministically() = runTest {
        val fixture = RecommendationFixture(listOf(
            recommendationEvent("a"), recommendationEvent("b"),
            recommendationEvent("c", category = EventCategory.CULTURE),
            recommendationEvent("lejos", location = Location(4.8133, -75.6961)),
            recommendationEvent("pocos-cupos", capacity = 30, attendance = 29)
        ))
        val repository = LocalRecommendationRepository(fixture.events, fixture.users, now = { recommendationNow })
        val first = repository.recommend()
        assertEquals(first, repository.recommend())
        assertFalse(first.hasPersonalization)
        assertEquals(listOf("a", "c"), first.items.take(2).map { it.event.id })
        assertTrue(first.items.indexOfFirst { it.event.id == "b" } < first.items.indexOfFirst { it.event.id == "lejos" })
        assertTrue(first.items.indexOfFirst { it.event.id == "b" } < first.items.indexOfFirst { it.event.id == "pocos-cupos" })
    }
}
