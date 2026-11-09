package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.RequestResult
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.features.auth.recover.RecoverPasswordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** «Recordar este dispositivo» (DataStore, guía 13) y la recuperación de contraseña. */
class SessionUseCasesTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repos = TestRepositories(sessionUserId = null)
    private val uc = repos.useCases

    @Test
    fun loginSavesWhetherToRememberTheDevice() = runTest {
        assertNotNull(uc.login(DemoCredentials.email, DemoCredentials.password, rememberDevice = true))
        assertTrue(uc.sessionPreferences.rememberDevice.first())

        uc.login(DemoCredentials.email, DemoCredentials.password, rememberDevice = false)
        assertFalse(uc.sessionPreferences.rememberDevice.first())
    }

    @Test
    fun wrongCredentialsDoNotChangeThePreference() = runTest {
        uc.sessionPreferences.setRememberDevice(true)

        assertNull(uc.login(DemoCredentials.email, "Incorrecta123", rememberDevice = false))

        assertTrue(uc.sessionPreferences.rememberDevice.first())
    }

    @Test
    fun aSessionThatWasNotRememberedIsClosedWhenTheAppOpens() = runTest {
        uc.login(DemoCredentials.email, DemoCredentials.password, rememberDevice = false)

        uc.restoreSession()

        assertNull(repos.users.currentUser.value)
    }

    @Test
    fun aRememberedSessionStaysOpen() = runTest {
        uc.login(DemoCredentials.email, DemoCredentials.password, rememberDevice = true)

        uc.restoreSession()

        assertEquals(TestRepositories.DEMO_USER, repos.users.currentUser.value?.id)
    }

    @Test
    fun aNewAccountIsRemembered() = runTest {
        val result = uc.registerUser("Ana Gómez", "ana@ejemplo.com", "Armenia, Quindío", "Comunidad1", emptySet(), "Parque", Location(4.53, -75.68))

        assertTrue(result is RegisterResult.Success)
        assertTrue(uc.sessionPreferences.rememberDevice.first())
    }

    @Test
    fun recoveringThePasswordValidatesTheEmailAndSendsTheLink() = runTest {
        val viewModel = RecoverPasswordViewModel(uc.sendPasswordReset, TestResourceProvider)

        viewModel.sendRecoveryLink()
        assertEquals(RequestResult.Failure("Ingresa tu correo electrónico"), viewModel.uiState.value.result)

        viewModel.onEmailChange("camilo.rodriguez@akjevents.co")
        viewModel.sendRecoveryLink()
        assertEquals(
            RequestResult.Success("Si el correo tiene una cuenta, le enviamos un enlace de recuperación. Revisa tu bandeja de entrada y el correo no deseado."),
            viewModel.uiState.value.result
        )
    }
}
