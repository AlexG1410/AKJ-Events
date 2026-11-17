package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.TestRepositories.Companion.DEMO_USER
import co.edu.uniquindio.akjevents.data.demo.DemoCredentials
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import co.edu.uniquindio.akjevents.domain.usecase.user.DeleteAccountUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class DeleteAccountUseCaseTest {
    @Test fun rejectedIdentityNeverStartsCleanup() = runTest {
        val repos = TestRepositories()
        var prepared = false
        val users = object : UserRepository by repos.users {
            override suspend fun reauthenticate(userId: String, password: String) = false
            override suspend fun beginAccountDeletion(userId: String) { prepared = true }
        }
        val events = repos.events.events.value
        assertFalse(DeleteAccountUseCase(users, repos.events, repos.notifications)(DEMO_USER, "wrong"))
        assertFalse(prepared)
        assertEquals(events, repos.events.events.value)
        assertNotNull(repos.users.findById(DEMO_USER))
    }

    @Test fun cleanupFailureRetainsIdentityAndCanBeRetried() = runTest {
        val repos = TestRepositories()
        val events = object : EventRepository by repos.events {
            override suspend fun removeUserData(userId: String) { error("INJECTED_CLEANUP_FAILURE") }
        }
        try { DeleteAccountUseCase(repos.users, events, repos.notifications)(DEMO_USER, DemoCredentials.password); fail() }
        catch (_: IllegalStateException) { }
        assertNotNull(repos.users.findById(DEMO_USER))
        assertEquals(DEMO_USER, repos.users.currentUser.value?.id)
        assertTrue(repos.useCases.deleteAccount(DEMO_USER, DemoCredentials.password))
        assertNull(repos.users.findById(DEMO_USER))
    }

    @Test fun cancellationDuringIdentityCheckPropagatesWithoutWrites() = runTest {
        val repos = TestRepositories()
        val before = repos.events.events.value
        val users = object : UserRepository by repos.users {
            override suspend fun reauthenticate(userId: String, password: String): Boolean = throw CancellationException()
        }
        try { DeleteAccountUseCase(users, repos.events, repos.notifications)(DEMO_USER, "unused"); fail() }
        catch (_: CancellationException) { }
        assertEquals(before, repos.events.events.value)
        assertNotNull(repos.users.findById(DEMO_USER))
    }
}
