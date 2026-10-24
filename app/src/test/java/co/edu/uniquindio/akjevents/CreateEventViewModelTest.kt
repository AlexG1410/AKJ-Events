package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.EventStatus
import co.edu.uniquindio.akjevents.domain.model.Location
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
    private val repos = TestRepositories()
    private val repository = repos.events

    // Un usuario recién registrado, sin eventos propios, abre la sesión
    private val currentUserId = (repos.users.register("Ana Gómez", "ana@ejemplo.com", "Armenia, Quindío", "Comunidad1", emptySet(), "Parque de la Vida", Location(4.5511, -75.6598)) as RegisterResult.Success).user.id
    private val viewModel by lazy { CreateEventViewModel(repository, repos.users, now = { now }) }
    private val parqueDeLaVida = PickedLocation("Parque de la Vida, Av. Bolívar", Location(4.5511, -75.6598))

    private fun fillValidForm() = with(viewModel) {
        onTitleChange("Taller de huertos urbanos")
        selectCategory(EventCategory.VOLUNTEERING)
        onDescriptionChange("Aprenderemos a cultivar en balcones y terrazas.")
        onPlaceChange("Parque de la Vida, Armenia")
        onLocationPicked(parqueDeLaVida)
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
        assertEquals(parqueDeLaVida.location, created.location)
        assertNull(repository.findPublicEvent(created.id))
        assertEquals(8, repository.publicEvents.value.size)
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
        assertEquals("Elige la ubicación del evento en el mapa", errorMessage())

        viewModel.onLocationPicked(parqueDeLaVida)
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

    @Test
    fun pickedLocationFillsAnEmptyPlace() {
        viewModel.onLocationPicked(parqueDeLaVida)

        assertEquals(parqueDeLaVida.address, viewModel.uiState.value.place)
        assertEquals(parqueDeLaVida.location, viewModel.uiState.value.location)
    }

    @Test
    fun editingARejectedEventResubmitsItForReview() {
        val demo = TestRepositories()
        val edit = CreateEventViewModel(demo.events, demo.users, eventId = "voleibol-callejero", now = { now })
        assertTrue(edit.uiState.value.isEditing)
        assertEquals("Torneo Nocturno de Voleibol Callejero", edit.uiState.value.title)

        edit.onTitleChange("Torneo de Voleibol en la cancha comunal")
        edit.onStartsAtChange(now.plusDays(3).withHour(15))
        edit.onEndsAtChange(now.plusDays(3).withHour(18))
        edit.submit()

        assertEquals(RequestResult.Success(CreateEventViewModel.RESUBMITTED_MESSAGE), edit.uiState.value.result)
        val event = demo.events.findEvent("voleibol-callejero")!!
        assertEquals("Torneo de Voleibol en la cancha comunal", event.title)
        assertEquals(EventStatus.PENDING, event.status)
        assertNull(event.rejectionReason)
    }

    @Test
    fun othersCannotEditAnEvent() {
        val demo = TestRepositories()
        val edit = CreateEventViewModel(demo.events, demo.users, eventId = "ciclopaseo", now = { now })

        assertTrue(!edit.uiState.value.canSubmit)
        edit.submit()
        assertEquals("Ciclopaseo nocturno por Armenia", demo.events.findEvent("ciclopaseo")!!.title)
    }

    @Test
    fun editingRejectsCapacityBelowLatestAttendanceAndAllowsEqualCapacity() {
        val demo = TestRepositories()
        val edit = CreateEventViewModel(demo.events, demo.users, eventId = "cine-foro", now = { now })
        val original = demo.events.findEvent("cine-foro")!!
        edit.onStartsAtChange(now.plusDays(3))
        edit.onEndsAtChange(now.plusDays(3).plusHours(2))
        edit.onLimitCapacityChange(true)
        edit.onCapacityChange(original.attendanceCount.toString())
        demo.events.confirmAttendance(original.id, "organizer-1")
        val latestCount = demo.events.findEvent(original.id)!!.attendanceCount
        assertEquals(original.attendanceCount + 1, latestCount)

        edit.submit()

        assertEquals("El cupo máximo no puede ser menor que los asistentes confirmados",
            (edit.uiState.value.result as RequestResult.Failure).errorMessage)
        assertEquals(EventStatus.VERIFIED, demo.events.findEvent(original.id)!!.status)
        edit.onCapacityChange(latestCount.toString())
        edit.submit()
        assertTrue(edit.uiState.value.result is RequestResult.Success)
        assertEquals(latestCount, demo.events.findEvent(original.id)!!.capacity)
    }
}
