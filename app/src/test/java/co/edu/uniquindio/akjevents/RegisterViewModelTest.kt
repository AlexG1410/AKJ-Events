package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.features.auth.register.PasswordStrength
import co.edu.uniquindio.akjevents.features.auth.register.RegisterViewModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel by lazy { RegisterViewModel(simulatedRequestMs = 0) }

    private fun fillForm(password: String, confirmation: String = password) = with(viewModel) {
        onFullNameChange("Ana Gómez")
        onEmailChange("ana.gomez@ejemplo.com")
        onCityChange("Armenia, Quindío")
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
    }
}
