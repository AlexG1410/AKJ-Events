package co.edu.uniquindio.akjevents

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.component.EventPhotoGallery
import co.edu.uniquindio.akjevents.core.component.PhotoGalleryMode
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.features.home.FeaturedEventCard
import co.edu.uniquindio.akjevents.features.home.UpcomingEventCard
import java.io.File
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Fotos locales y callbacks contados: sin Firebase, Cloudinary ni cuota IA. */
class EventPhotoGallerySmokeTest {
    @get:Rule val compose = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)
    private fun photo(color: Int): String {
        val file = File(context.cacheDir, "gallery-$color.png")
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(color) }
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file.toURI().toString()
    }
    private fun event(id: String = "a", count: Int = 3) = CommunityEvent(
        id = id, organizerId = "organizer", title = "Evento $id", category = EventCategory.CULTURE,
        description = "Descripción local", address = "Lugar", location = Location(4.5, -75.6),
        imageUrls = if (count == 2) listOf(photo(android.graphics.Color.RED), photo(android.graphics.Color.BLUE)) else listOf(photo(android.graphics.Color.RED), "file:///nonexistent-photo.png", photo(android.graphics.Color.BLUE)).take(count),
        startsAt = LocalDateTime.now().plusDays(1), endsAt = LocalDateTime.now().plusDays(1).plusHours(2), status = EventStatus.VERIFIED
    )
    private fun position(id: String, page: Int, total: Int) = compose.onNodeWithTag("event-gallery-$id", useUnmergedTree = true)
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, context.getString(R.string.gallery_position, page, total)))
    private fun waitPage(id: String, page: Int, total: Int) {
        val expected = context.getString(R.string.gallery_position, page, total)
        compose.waitUntil(8_000) { compose.onNodeWithTag("event-gallery-$id", useUnmergedTree = true).fetchSemanticsNode().config[SemanticsProperties.StateDescription] == expected }
    }

    @Test fun singlePhotoHasNoControlsAndStartsAtCover() {
        val event = event(count = 1)
        compose.setContent { AKJEventsTheme { EventPhotoGallery(event, Modifier.size(300.dp, 200.dp)) } }
        position("a", 1, 1)
        compose.onNodeWithContentDescription(text(R.string.gallery_next)).assertDoesNotExist()
        compose.onNodeWithContentDescription(text(R.string.gallery_previous)).assertDoesNotExist()
    }

    @Test fun listSwipesInOrderAndBrokenPhotoDoesNotBlockTheNextOne() {
        val event = event()
        var opens = 0
        compose.setContent { AKJEventsTheme { Surface(onClick = { opens++ }) { EventPhotoGallery(event, Modifier.size(300.dp, 200.dp), PhotoGalleryMode.LIST) } } }
        position("a", 1, 3)
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { swipeLeft() }
        waitPage("a", 2, 3)
        compose.onNodeWithContentDescription(text(R.string.gallery_next)).performClick()
        waitPage("a", 3, 3)
        assertEquals(0, opens)
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { click(center) }
        assertEquals(1, opens)
    }

    @Test fun horizontalRowArrowChangesPhotoSwipeChangesEventAndActionsStaySeparate() {
        val events = listOf(event("a"), event("b", 2))
        var opened: String? = null
        var opens = 0
        var interests = 0
        compose.setContent { AKJEventsTheme { LazyRow(Modifier.fillMaxWidth()) { items(events, key = { it.id }) { event ->
            FeaturedEventCard(event, false, { opened = event.id; opens++ }, { interests++ }, Modifier.width(300.dp))
        } } } }
        compose.onAllNodesWithContentDescription(text(R.string.gallery_next))[0].performClick()
        waitPage("a", 2, 3)
        assertEquals(null, opened)
        assertEquals(0, interests)
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { swipeLeft(startX = width * 0.8f, endX = width * 0.1f) }
        compose.onNodeWithText("Evento b").assertIsDisplayed().performClick()
        assertEquals("b", opened)
        assertEquals(1, opens)
        compose.onAllNodesWithText(text(R.string.home_sign_up_free)).onLast().performClick()
        assertEquals(2, opens)
        compose.onAllNodesWithContentDescription(text(R.string.interest_add)).onLast().performClick()
        assertEquals(1, interests)
        assertEquals("b", opened)
        assertEquals(2, opens)
    }

    @Test fun compactKeepsDateAndUsesOneSmallPhotoControlWithoutOpeningOrVoting() {
        val event = event()
        var opens = 0
        var votes = 0
        compose.setContent { AKJEventsTheme { UpcomingEventCard(event, false, 1.0, { opens++ }, { votes++ }) } }
        compose.onNodeWithText("1/3").performClick()
        waitPage("a", 2, 3)
        assertEquals(0, opens)
        assertEquals(0, votes)
        compose.onNodeWithText("Evento a").performClick()
        assertEquals(1, opens)
        compose.onNodeWithText(text(R.string.interest_add)).performClick()
        assertEquals(1, votes)
        assertEquals(1, opens)
    }

    @Test(timeout = 60_000) fun detailAutomaticallyAdvancesCanPauseAndResumeAndWraps() {
        compose.mainClock.autoAdvance = false
        val event = event(count = 2)
        compose.setContent { AKJEventsTheme { EventPhotoGallery(event, Modifier.size(300.dp, 200.dp), PhotoGalleryMode.DETAIL) } }
        compose.mainClock.advanceTimeByFrame()
        position("a", 1, 2)
        compose.mainClock.advanceTimeBy(5_600)
        position("a", 2, 2)
        compose.onNodeWithContentDescription(text(R.string.gallery_pause)).performClick()
        compose.mainClock.advanceTimeBy(5_500)
        position("a", 2, 2)
        compose.onNodeWithContentDescription(text(R.string.gallery_resume)).performClick()
        compose.mainClock.advanceTimeBy(5_600)
        position("a", 1, 2)
    }

    @Test(timeout = 60_000) fun detailStopsInBackgroundAndWhenScrolledOutOfView() {
        compose.mainClock.autoAdvance = false
        val event = event(count = 2)
        lateinit var owner: LifecycleOwner
        lateinit var registry: LifecycleRegistry
        compose.runOnUiThread {
            owner = object : LifecycleOwner { override val lifecycle: Lifecycle get() = registry }
            registry = LifecycleRegistry(owner).apply { currentState = Lifecycle.State.RESUMED }
        }
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { AKJEventsTheme {
            Column(Modifier.height(220.dp).verticalScroll(rememberScrollState())) {
                EventPhotoGallery(event, Modifier.size(300.dp, 200.dp), PhotoGalleryMode.DETAIL)
                Box(Modifier.height(600.dp).fillMaxWidth())
            }
        } } }
        compose.mainClock.advanceTimeByFrame()
        compose.runOnUiThread { registry.currentState = Lifecycle.State.STARTED }
        compose.mainClock.advanceTimeBy(5_500)
        position("a", 1, 2)
        compose.runOnUiThread { registry.currentState = Lifecycle.State.RESUMED }
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { swipeUp() }
        compose.mainClock.advanceTimeBy(5_500)
        position("a", 1, 2)
    }

    @Test(timeout = 60_000) fun detailHoldingTouchPausesAndReleaseRestartsFiveSecondWait() {
        compose.mainClock.autoAdvance = false
        val event = event(count = 2)
        compose.setContent { AKJEventsTheme { EventPhotoGallery(event, Modifier.size(300.dp, 200.dp), PhotoGalleryMode.DETAIL) } }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(6_000)
        position("a", 1, 2)
        compose.onNodeWithTag("event-gallery-a", useUnmergedTree = true).performTouchInput { up() }
        compose.mainClock.advanceTimeBy(4_800)
        position("a", 1, 2)
        compose.mainClock.advanceTimeBy(900)
        position("a", 2, 2)
    }

    @Test fun reorderingKeepsPhotoStateByEventIdAndANewEventStartsAtItsCover() {
        var events by mutableStateOf(listOf(event("a"), event("b", 2)))
        compose.setContent { AKJEventsTheme { LazyRow(Modifier.fillMaxWidth().testTag("gallery-row")) {
            items(events, key = { it.id }) { event -> EventPhotoGallery(event, Modifier.size(280.dp, 150.dp)) }
        } } }
        compose.onAllNodesWithContentDescription(text(R.string.gallery_next))[0].performClick()
        waitPage("a", 2, 3)
        compose.runOnUiThread { events = events.reversed() }
        position("a", 2, 3)
        val added = event("c", 2)
        compose.runOnUiThread { events = listOf(added) + events }
        compose.onNodeWithTag("gallery-row").performScrollToIndex(0)
        position("c", 1, 2)
    }
}
