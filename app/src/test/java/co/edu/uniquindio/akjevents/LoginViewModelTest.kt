package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.repository.InMemorySessionPreferences
import co.edu.uniquindio.akjevents.data.repository.InMemoryUserRepository
import co.edu.uniquindio.akjevents.domain.usecase.auth.LoginUseCase
import co.edu.uniquindio.akjevents.features.auth.login.LoginViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel by lazy { LoginViewModel(LoginUseCase(InMemoryUserRepository(), InMemorySessionPreferences()), TestResourceProvider) }

    @Test
    fun demoCredentialsBelongToTheDemoUser() = runTest {
        assertEquals(SampleUsers.demoUser.email, DemoCredentials.email)
    }

    @Test
    fun validDemoCredentialsReturnSuccess() = runTest {
        viewModel.onEmailChange("  ${DemoCredentials.email.uppercase()}  ")
        viewModel.onPasswordChange(DemoCredentials.password)

        assertTrue(viewModel.uiState.value.isFormValid)
        viewModel.login()

        assertTrue(viewModel.uiState.value.result is RequestResult.Success)
        assertTrue(!viewModel.uiState.value.isModerator)
    }

    @Test
    fun moderatorCredentialsAreFlaggedForTheModerationPanel() = runTest {
        viewModel.onEmailChange(DemoCredentials.moderatorEmail)
        viewModel.onPasswordChange(DemoCredentials.moderatorPassword)

        viewModel.login()

        assertTrue(viewModel.uiState.value.result is RequestResult.Success)
        assertTrue(viewModel.uiState.value.isModerator)
    }

    @Test
    fun incorrectCredentialsReturnFailure() = runTest {
        viewModel.onEmailChange(DemoCredentials.email)
        viewModel.onPasswordChange("ClaveIncorrecta123")

        viewModel.login()

        assertEquals(
            "Correo o contraseña incorrectos",
            (viewModel.uiState.value.result as RequestResult.Failure).errorMessage
        )
    }

    @Test
    fun malformedEmailIsRejectedBeforeAuthentication() = runTest {
        viewModel.onEmailChange("correo-invalido")
        viewModel.onPasswordChange(DemoCredentials.password)

        assertTrue(!viewModel.uiState.value.isFormValid)
        viewModel.login()

        assertTrue(viewModel.uiState.value.result is RequestResult.Failure)
    }
}
