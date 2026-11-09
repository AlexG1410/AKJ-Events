package co.edu.uniquindio.akjevents.di

import co.edu.uniquindio.akjevents.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Instancias de Firebase (guías 19 y 20). Con `-PuseFirebaseEmulator=true` se conectan al emulador
 * local de Firebase en [BuildConfig.FIREBASE_EMULATOR_HOST] (por defecto 10.0.2.2, el computador visto
 * desde el emulador de Android).
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    private val EMULATOR_HOST = BuildConfig.FIREBASE_EMULATOR_HOST
    private const val AUTH_EMULATOR_PORT = 9099
    private const val FIRESTORE_EMULATOR_PORT = 8080

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, AUTH_EMULATOR_PORT)
        // Los correos de Firebase (por ejemplo, el de recuperar la contraseña) salen en el idioma del teléfono
        useAppLanguage()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (BuildConfig.USE_FIREBASE_EMULATOR) useEmulator(EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
    }
}
