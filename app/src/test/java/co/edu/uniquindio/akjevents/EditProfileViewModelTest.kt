package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.features.profile.edit.EditProfileViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories()
    private val viewModel by lazy { EditProfileViewModel(repos.users, repos.events) }

    @Test
    fun savesPersonalDataLocationAndInterests() {
        viewModel.onNameChange("Camilo Andrés Rodríguez")
        viewModel.onLocationPicked(PickedLocation("Plaza principal de Calarcá", Location(4.5296, -75.6431)))
        viewModel.toggleInterest(EventCategory.ACADEMIC)

        viewModel.save()

        assertTrue(viewModel.uiState.value.result is RequestResult.Success)
        val user = repos.users.findById(DEMO_USER)!!
        assertEquals("Camilo Andrés Rodríguez", user.name)
        assertEquals("Calarcá, Quindío", user.city)
        assertEquals(Location(4.5296, -75.6431), user.location)
        assertTrue(EventCategory.ACADEMIC in user.interests)
    }

    @Test
    fun invalidOrTakenEmailIsRejected() {
        viewModel.onEmailChange("correo-invalido")
        viewModel.save()
        assertTrue(viewModel.uiState.value.result is RequestResult.Failure)

        viewModel.onFailureShown()
        viewModel.onEmailChange(DemoCredentials.moderatorEmail)
        viewModel.save()
        assertEquals("Ese correo ya pertenece a otra cuenta", (viewModel.uiState.value.result as RequestResult.Failure).errorMessage)
    }

    @Test
    fun deletingTheAccountRemovesTheUserAndTheirEvents() {
        viewModel.deleteAccount()

        assertTrue(viewModel.uiState.value.isAccountDeleted)
        assertNull(repos.users.findById(DEMO_USER))
        assertNull(repos.users.currentUser.value)
        assertTrue(repos.events.findEventsByOrganizer(DEMO_USER).isEmpty())
    }
}
