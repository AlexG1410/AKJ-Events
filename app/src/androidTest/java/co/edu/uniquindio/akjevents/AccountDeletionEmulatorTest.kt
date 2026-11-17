package co.edu.uniquindio.akjevents

import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.data.remote.event.EventDto
import co.edu.uniquindio.akjevents.data.repository.remote.FirebaseUserRepository
import co.edu.uniquindio.akjevents.data.repository.remote.FirestoreEventRepository
import co.edu.uniquindio.akjevents.data.repository.remote.FirestoreNotificationRepository
import co.edu.uniquindio.akjevents.domain.model.*
import co.edu.uniquindio.akjevents.domain.repository.EventRepository
import co.edu.uniquindio.akjevents.domain.usecase.user.DeleteAccountUseCase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime
import java.util.UUID

/** Solo instancia Firebase nombrada demo; jamás usa el FirebaseApp de producción/Hilt. */
class AccountDeletionEmulatorTest {
    private fun fixture(test: suspend (FirebaseAuth, FirebaseFirestore, FirebaseUserRepository, FirestoreEventRepository, FirestoreNotificationRepository, String, String, String) -> Unit) = runBlocking {
        val args = InstrumentationRegistry.getArguments()
        check(args.getString("localDeletionEmulators") == "true") { "Run through firebase-tests npm run test:android" }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = FirebaseApp.initializeApp(context, FirebaseOptions.Builder()
            .setProjectId("demo-akj-checkin").setApplicationId("1:123:android:demo")
            .setApiKey("demo-only-${UUID.randomUUID()}").build(), "deletion-${UUID.randomUUID()}")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val auth = FirebaseAuth.getInstance(app).apply { useEmulator("127.0.0.1", 9199) }
        val db = FirebaseFirestore.getInstance(app).apply { useEmulator("127.0.0.1", 8180) }
        try {
            val email = "${UUID.randomUUID()}@example.test"
            val password = "LocalOnly-${UUID.randomUUID()}"
            val uid = auth.createUserWithEmailAndPassword(email, password).await().user!!.uid
            db.collection("users").document(uid).set(mapOf("id" to uid, "role" to "USER", "name" to "Disposable", "email" to email, "points" to 0, "badges" to emptyList<String>())).await()
            val event = CommunityEvent("delete-$uid", uid, "Disposable event", EventCategory.SOCIAL, "Description", "Local", Location(0.0, 0.0), listOf("https://example.test/image.jpg"), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1))
            db.collection("events").document(event.id).set(EventDto.fromDomain(event)).await()
            val users = FirebaseUserRepository(auth, db, scope)
            val events = FirestoreEventRepository(db, users, scope)
            val notifications = FirestoreNotificationRepository(db, users, scope)
            test(auth, db, users, events, notifications, uid, email, password)
        } finally {
            scope.cancel()
            db.terminate().await()
            app.delete()
        }
    }

    @Test fun wrongPasswordCannotStartCleanup() = fixture { _, db, users, events, notifications, uid, _, _ ->
        assertFalse(DeleteAccountUseCase(users, events, notifications)(uid, "wrong-password"))
        assertTrue(db.collection("users").document(uid).get().await().exists())
        assertNull(db.collection("users").document(uid).get().await().getBoolean("deletionPending"))
        assertTrue(db.collection("events").document("delete-$uid").get().await().exists())
    }

    @Test fun cleanupFailureKeepsAccountAndRetryDeletesWithRealAndroidRepositories() = fixture { auth, db, users, events, notifications, uid, email, password ->
        val failing = object : EventRepository by events {
            override suspend fun removeUserData(userId: String) { error("INJECTED_CLEANUP_FAILURE") }
        }
        try { DeleteAccountUseCase(users, failing, notifications)(uid, password); fail("Expected cleanup failure") } catch (_: IllegalStateException) { }
        assertEquals(uid, auth.currentUser?.uid)
        assertEquals(true, db.collection("users").document(uid).get().await().getBoolean("deletionPending"))
        assertTrue(DeleteAccountUseCase(users, events, notifications)(uid, password))
        assertNull(auth.currentUser)
        try { auth.signInWithEmailAndPassword(email, password).await(); fail("Deleted Auth account must not sign in") } catch (_: com.google.firebase.auth.FirebaseAuthException) { }
    }
}
