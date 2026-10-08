package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.features.auth.login.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel by lazy { LoginViewModel(simulatedRequestMs = 0) }

    @Test
    fun demoCredentialsBelongToCurrentUser() {
        assertEquals(SampleUsers.currentUser.email, DemoCredentials.email)
    }

    @Test
    fun validDemoCredentialsReturnSuccess() {
        viewModel.onEmailChange("  ${DemoCredentials.email.uppercase()}  ")
        viewModel.onPasswordChange(DemoCredentials.password)

        assertTrue(viewModel.uiState.value.isFormValid)
        viewModel.login()

        assertTrue(viewModel.uiState.value.result is RequestResult.Success)
    }

    @Test
    fun incorrectCredentialsReturnFailure() {
        viewModel.onEmailChange(DemoCredentials.email)
        viewModel.onPasswordChange("ClaveIncorrecta123")

        viewModel.login()

        assertEquals(
            "Correo o contraseña incorrectos",
            (viewModel.uiState.value.result as RequestResult.Failure).errorMessage
        )
    }

    @Test
    fun malformedEmailIsRejectedBeforeAuthentication() {
        viewModel.onEmailChange("correo-invalido")
        viewModel.onPasswordChange(DemoCredentials.password)

        assertTrue(!viewModel.uiState.value.isFormValid)
        viewModel.login()

        assertTrue(viewModel.uiState.value.result is RequestResult.Failure)
    }
}
