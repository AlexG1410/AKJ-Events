package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.model.UserLevel
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.features.auth.register.PasswordStrength
import co.edu.uniquindio.akjevents.features.auth.register.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val users = InMemoryUserRepository()
    private val viewModel by lazy { RegisterViewModel(users, simulatedRequestMs = 0) }
    private val picked = PickedLocation("Parque Sucre, Cra. 14 # 13-00", Location(4.5382, -75.6731))

    private fun fillForm(password: String, confirmation: String = password) = with(viewModel) {
        onFullNameChange("Ana Gómez")
        onEmailChange("ana.gomez@ejemplo.com")
        onCityChange("Armenia, Quindío")
        onLocationPicked(picked)
        onPasswordChange(password)
        onConfirmPasswordChange(confirmation)
        onAcceptedRulesChange(true)
    }

    @Test
    fun weakPasswordCannotRegister() {
        fillForm(password = "comunidad1")

        assertTrue(viewModel.uiState.value.passwordStrength != PasswordStrength.STRONG)
        assertFalse(viewModel.uiState.value.isFormValid)

        viewModel.register()
        assertTrue(viewModel.uiState.value.result is RequestResult.Failure)
    }

    @Test
    fun differentPasswordConfirmationCannotRegister() {
        fillForm(password = "Comunidad1", confirmation = "Comunidad2")

        assertFalse(viewModel.uiState.value.isFormValid)
        assertTrue(viewModel.uiState.value.confirmPasswordError != null)

        viewModel.register()
        assertTrue(viewModel.uiState.value.result is RequestResult.Failure)
    }

    @Test
    fun completeValidFormCanRegister() {
        fillForm(password = "Comunidad1")

        assertTrue(viewModel.uiState.value.isFormValid)
        assertTrue(viewModel.uiState.value.passwordStrength == PasswordStrength.STRONG)

        viewModel.register()
        assertTrue(viewModel.uiState.value.result is RequestResult.Success)
        val created = users.currentUser.value!!
        assertEquals("ana.gomez@ejemplo.com", created.email)
        assertEquals(UserLevel.SPECTATOR, created.level)
        assertEquals(picked.address, created.address)
        assertEquals(picked.location, created.location)
    }

    @Test
    fun emailAlreadyInUseCannotRegister() {
        fillForm(password = "Comunidad1")
        viewModel.onEmailChange(DemoCredentials.email)

        viewModel.register()

        assertEquals("Ya existe una cuenta con ese correo", (viewModel.uiState.value.result as RequestResult.Failure).errorMessage)
    }

    @Test
    fun selectingACityAloneDoesNotReplacePickingAnAddress() {
        fillForm("Comunidad1")
        viewModel.onCityChange("Calarcá, Quindío")

        assertFalse(viewModel.uiState.value.isFormValid)
        assertEquals(null, viewModel.uiState.value.location)
        assertEquals("", viewModel.uiState.value.address)
        viewModel.register()
        assertEquals("Selecciona tu dirección en el mapa", (viewModel.uiState.value.result as RequestResult.Failure).errorMessage)
        assertEquals(null, users.currentUser.value)
    }

    @Test
    fun mapSelectionUpdatesCityAndCanBeChangedWithoutLosingOtherFields() {
        fillForm("Comunidad1")
        viewModel.onCityChange("Armenia, Quindío")
        assertEquals(picked, viewModel.locationForPicker())
        val other = PickedLocation("Parque Bolívar de Calarcá", Location(4.5296, -75.6431))

        viewModel.onLocationPicked(other)

        assertEquals("Calarcá, Quindío", viewModel.uiState.value.city)
        assertEquals(other, viewModel.locationForPicker())
        assertEquals("Ana Gómez", viewModel.uiState.value.fullName)
        assertEquals("Comunidad1", viewModel.uiState.value.password)
        assertTrue(viewModel.uiState.value.isFormValid)
        viewModel.register()
        assertEquals(other.location, users.currentUser.value!!.location)
    }
}
