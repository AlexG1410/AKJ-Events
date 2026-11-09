package co.edu.uniquindio.akjevents.data.repository.remote

import co.edu.uniquindio.akjevents.data.demo.SamplePlaces
import co.edu.uniquindio.akjevents.data.demo.SampleUsers
import co.edu.uniquindio.akjevents.data.remote.user.PointsEntryDto
import co.edu.uniquindio.akjevents.data.remote.user.UserDto
import co.edu.uniquindio.akjevents.di.ApplicationScope
import co.edu.uniquindio.akjevents.domain.model.Badge
import co.edu.uniquindio.akjevents.domain.model.EventCategory
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.model.PointsEntry
import co.edu.uniquindio.akjevents.domain.model.PointsReason
import co.edu.uniquindio.akjevents.domain.model.User
import co.edu.uniquindio.akjevents.domain.model.UserRole
import co.edu.uniquindio.akjevents.domain.repository.PointsChange
import co.edu.uniquindio.akjevents.domain.repository.ProfileUpdateResult
import co.edu.uniquindio.akjevents.domain.repository.RegisterResult
import co.edu.uniquindio.akjevents.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cuentas con Firebase Authentication y perfiles en la colección `users` de Firestore (guías 19 y 20).
 * El id de cada perfil es el UID de Authentication. Firebase guarda la sesión en el teléfono, así que
 * sigue abierta al cerrar la app.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationScope scope: CoroutineScope
) : UserRepository {

    private val usersCollection = firestore.collection(USERS)
    private val historyCollection = firestore.collection(POINTS_HISTORY)

    /** UID de la sesión de Authentication; cambia al iniciar o cerrar sesión. */
    private val sessionUid: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    private val _isSessionReady = MutableStateFlow(false)
    override val isSessionReady: StateFlow<Boolean> = _isSessionReady.asStateFlow()

    // El perfil se escucha en tiempo real: puntos, insignias o cambios desde otro dispositivo se ven al instante
    override val currentUser: StateFlow<User?> = sessionUid
        .flatMapLatest { uid -> if (uid == null) flowOf(null) else profileUpdates(uid) }
        .onEach { _isSessionReady.value = true }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val pointsHistory: StateFlow<List<PointsEntry>> = sessionUid
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else historyUpdates(uid) }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun findById(id: String): User? =
        usersCollection.document(id).get().await().toUser()
            // Los eventos de muestra aún viven en memoria (pasan a Firestore en el paso 4) y sus
            // organizadores no tienen cuenta: se muestran con sus datos de ejemplo
            ?: SampleUsers.findById(id)

    override suspend fun login(email: String, password: String): User? {
        val result = try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return null // Correo o contraseña incorrectos
        } catch (e: FirebaseAuthInvalidUserException) {
            return null // La cuenta no existe o fue deshabilitada
        }
        val firebaseUser = result.user ?: return null
        return profileOf(firebaseUser)
    }

    override suspend fun logout() = auth.signOut()

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun register(
        name: String,
        email: String,
        city: String,
        password: String,
        interests: Set<EventCategory>,
        address: String,
        location: Location
    ): RegisterResult {
        // Firebase Authentication rechaza los correos repetidos: no hace falta consultarlos antes
        val result = try {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
        } catch (e: FirebaseAuthUserCollisionException) {
            return RegisterResult.EmailInUse
        }
        val firebaseUser = result.user ?: error("Firebase no devolvió el usuario creado")
        val user = User(
            id = firebaseUser.uid,
            name = name.trim(),
            email = email.trim(),
            city = city,
            address = address.trim(),
            location = location,
            role = UserRole.USER,
            interests = interests
        )
        try {
            usersCollection.document(user.id).set(UserDto.fromDomain(user)).await()
        } catch (e: Exception) {
            // Si no se pudo guardar el perfil, se borra la cuenta para no dejarla a medias
            withContext(NonCancellable) { runCatching { firebaseUser.delete().await() } }
            throw e
        }
        return RegisterResult.Success(user)
    }

    override suspend fun updateProfile(updated: User): ProfileUpdateResult {
        val firebaseUser = auth.currentUser?.takeIf { it.uid == updated.id } ?: return ProfileUpdateResult.NOT_FOUND
        val existing = usersCollection.document(updated.id).get().await().toUser() ?: return ProfileUpdateResult.NOT_FOUND
        // Solo los datos personales; el correo cambia cuando la persona confirma el enlace (se sincroniza al iniciar sesión)
        val saved = existing.copy(
            name = updated.name.trim(),
            city = updated.city,
            address = updated.address,
            location = updated.location,
            interests = updated.interests,
            profilePictureUrl = updated.profilePictureUrl
        )
        usersCollection.document(saved.id).set(UserDto.fromDomain(saved)).await()

        val newEmail = updated.email.trim()
        if (newEmail.equals(existing.email, ignoreCase = true)) return ProfileUpdateResult.UPDATED
        return try {
            firebaseUser.verifyBeforeUpdateEmail(newEmail).await()
            ProfileUpdateResult.EMAIL_VERIFICATION_SENT
        } catch (e: FirebaseAuthUserCollisionException) {
            ProfileUpdateResult.EMAIL_IN_USE
        }
    }

    override suspend fun deleteAccount(userId: String): Boolean {
        val firebaseUser = auth.currentUser?.takeIf { it.uid == userId } ?: return false
        val history = historyCollection.whereEqualTo(FIELD_USER_ID, userId).get().await()
        firestore.runBatch { batch ->
            history.documents.forEach { batch.delete(it.reference) }
            batch.delete(usersCollection.document(userId))
        }.await()
        return try {
            firebaseUser.delete().await()
            true
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            // Firebase pide un inicio de sesión reciente para borrar la cuenta: se cierra la sesión para reintentar
            auth.signOut()
            false
        }
    }

    override suspend fun addPoints(userId: String, points: Int, reason: PointsReason, eventTitle: String?): PointsChange? {
        val reference = usersCollection.document(userId)
        return firestore.runTransaction { transaction ->
            val user = transaction.get(reference).toUser() ?: return@runTransaction null
            val updated = user.copy(points = (user.points + points).coerceAtLeast(0))
            transaction.update(reference, FIELD_POINTS, updated.points)
            val entry = PointsEntry(userId, points, reason, LocalDateTime.now(), eventTitle)
            transaction.set(historyCollection.document(), PointsEntryDto.fromDomain(entry))
            PointsChange(before = user, after = updated)
        }.await()
    }

    override suspend fun addBadge(userId: String, badge: Badge): Boolean {
        val reference = usersCollection.document(userId)
        return firestore.runTransaction { transaction ->
            val user = transaction.get(reference).toUser() ?: return@runTransaction false
            if (badge in user.badges) return@runTransaction false
            transaction.update(reference, FIELD_BADGES, FieldValue.arrayUnion(badge.name))
            true
        }.await()
    }

    /**
     * Perfil de la cuenta que acaba de iniciar sesión. Si no existe (por ejemplo, un moderador creado en la
     * consola de Firebase), se crea con el rol Usuario; el rol de moderador se asigna en la consola.
     */
    private suspend fun profileOf(firebaseUser: FirebaseUser): User {
        val reference = usersCollection.document(firebaseUser.uid)
        val stored = reference.get().await().toUser()
        val authEmail = firebaseUser.email.orEmpty()
        if (stored == null) {
            val city = SamplePlaces.cities.first()
            val user = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: authEmail.substringBefore("@"),
                email = authEmail,
                city = city.name,
                address = city.name,
                location = city.location
            )
            reference.set(UserDto.fromDomain(user)).await()
            return user
        }
        // El correo confirmado en Authentication manda sobre el guardado en el perfil
        if (authEmail.isNotBlank() && !stored.email.equals(authEmail, ignoreCase = true)) {
            reference.update(FIELD_EMAIL, authEmail).await()
            return stored.copy(email = authEmail)
        }
        return stored
    }

    private fun profileUpdates(uid: String): Flow<User?> = callbackFlow {
        val registration = usersCollection.document(uid).addSnapshotListener { snapshot, _ ->
            trySend(snapshot?.toUser())
        }
        awaitClose { registration.remove() }
    }

    private fun historyUpdates(uid: String): Flow<List<PointsEntry>> = callbackFlow {
        val registration = historyCollection.whereEqualTo(FIELD_USER_ID, uid).addSnapshotListener { snapshot, _ ->
            val entries = snapshot?.documents.orEmpty()
                .mapNotNull { it.toObject(PointsEntryDto::class.java)?.toDomain() }
                .sortedByDescending { it.createdAt }
            trySend(entries)
        }
        awaitClose { registration.remove() }
    }

    private fun DocumentSnapshot.toUser(): User? = toObject(UserDto::class.java)?.copy(id = id)?.toDomain()

    private companion object {
        const val USERS = "users"
        const val POINTS_HISTORY = "pointsHistory"
        const val FIELD_USER_ID = "userId"
        const val FIELD_POINTS = "points"
        const val FIELD_BADGES = "badges"
        const val FIELD_EMAIL = "email"
    }
}
