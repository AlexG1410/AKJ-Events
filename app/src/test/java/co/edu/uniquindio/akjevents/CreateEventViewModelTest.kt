package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemoryEventRepository
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.features.event.create.CreateEventViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class CreateEventViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = LocalDateTime.of(2026, 10, 5, 12, 0)
    private val repository = InMemoryEventRepository()
    private val viewModel by lazy { CreateEventViewModel(repository, now = { now }) }

    private val currentUserId = SampleUsers.currentUser.id

    private fun fillValidForm() = with(viewModel) {
        onTitleChange("Taller de huertos urbanos")
        selectCategory(EventCategory.VOLUNTEERING)
        onDescriptionChange("Aprenderemos a cultivar en balcones y terrazas.")
        onPlaceChange("Parque de la Vida, Armenia")
        onStartsAtChange(now.plusDays(2).withHour(9))
        onEndsAtChange(now.plusDays(2).withHour(12))
    }

    private fun errorMessage() = (viewModel.uiState.value.result as? RequestResult.Failure)?.errorMessage

    @Test
    fun validFormCreatesPendingEventThatIsNotPublic() {
        fillValidForm()
        viewModel.onLimitCapacityChange(true)
        viewModel.onCapacityChange("35")

        viewModel.submit()

        assertEquals(RequestResult.Success(CreateEventViewModel.SUBMITTED_MESSAGE), viewModel.uiState.value.result)
        val created = repository.findEventsByOrganizer(currentUserId).single()
        assertEquals(EventStatus.PENDING, created.status)
        assertEquals("Taller de huertos urbanos", created.title)
        assertEquals(35, created.capacity)
        assertEquals(listOf(viewModel.uiState.value.imageUrl), created.imageUrls)
        assertNull(repository.findPublicEvent(created.id))
        assertEquals(5, repository.publicEvents.value.size)
    }

    @Test
    fun unlimitedCapacityIsSavedAsNull() {
        fillValidForm()
        viewModel.onCapacityChange("0")

        viewModel.submit()

        assertNull(repository.findEventsByOrganizer(currentUserId).single().capacity)
    }

    @Test
    fun missingRequiredFieldsAreRejected() {
        viewModel.submit()
        assertEquals("Escribe el título del evento", errorMessage())

        viewModel.onTitleChange("Título")
        viewModel.submit()
        assertEquals("Elige una categoría", errorMessage())

        viewModel.selectCategory(EventCategory.SOCIAL)
        viewModel.submit()
        assertEquals("Escribe la descripción del evento", errorMessage())

        viewModel.onDescriptionChange("Descripción")
        viewModel.submit()
        assertEquals("Indica el lugar o punto de encuentro", errorMessage())

        viewModel.onPlaceChange("Plaza de Bolívar")
        viewModel.submit()
        assertEquals("Elige la fecha y hora de inicio y de fin", errorMessage())

        assertTrue(repository.findEventsByOrganizer(currentUserId).isEmpty())
    }

    @Test
    fun startMustBeInTheFuture() {
        fillValidForm()
        viewModel.onStartsAtChange(now.minusHours(1))

        viewModel.submit()

        assertEquals("La fecha de inicio debe ser futura", errorMessage())
        assertTrue(repository.findEventsByOrganizer(currentUserId).isEmpty())
    }

    @Test
    fun endMustBeAfterStart() {
        fillValidForm()
        viewModel.onEndsAtChange(now.plusDays(2).withHour(9))

        viewModel.submit()

        assertEquals("La fecha de fin debe ser posterior al inicio", errorMessage())
        assertTrue(repository.findEventsByOrganizer(currentUserId).isEmpty())
    }

    @Test
    fun limitedCapacityMustBePositive() {
        fillValidForm()
        viewModel.onLimitCapacityChange(true)
        viewModel.onCapacityChange("0")

        viewModel.submit()

        assertEquals("El cupo máximo debe ser mayor que 0", errorMessage())
        assertTrue(repository.findEventsByOrganizer(currentUserId).isEmpty())
    }

    @Test
    fun descriptionIsLimitedTo600Characters() {
        viewModel.onDescriptionChange("a".repeat(700))

        assertEquals(CreateEventViewModel.MAX_DESCRIPTION_LENGTH, viewModel.uiState.value.description.length)
    }

    @Test
    fun capacityButtonsNeverGoBelowZero() {
        viewModel.onCapacityChange("1")
        viewModel.decreaseCapacity()
        viewModel.decreaseCapacity()
        assertEquals("0", viewModel.uiState.value.capacity)

        viewModel.increaseCapacity()
        assertEquals("1", viewModel.uiState.value.capacity)
    }

    @Test
    fun changeImageUsesANewSeed() {
        val before = viewModel.uiState.value.imageUrl

        viewModel.changeImage()

        assertNotEquals(before, viewModel.uiState.value.imageUrl)
        assertTrue(viewModel.uiState.value.imageUrl.startsWith("https://picsum.photos/seed/"))
    }
}
