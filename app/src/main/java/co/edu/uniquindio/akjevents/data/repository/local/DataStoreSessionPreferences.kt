package co.edu.uniquindio.akjevents.data.repository.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Archivo de preferencias "session" (guía 13)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/** Guarda en el teléfono si la persona marcó «Recordar este dispositivo» al iniciar sesión. */
@Singleton
class DataStoreSessionPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SessionPreferencesRepository {

    private object Keys {
        val REMEMBER_DEVICE = booleanPreferencesKey("remember_device")
    }

    override val rememberDevice: Flow<Boolean> = context.dataStore.data.map { it[Keys.REMEMBER_DEVICE] ?: false }

    override suspend fun setRememberDevice(remember: Boolean) {
        context.dataStore.edit { it[Keys.REMEMBER_DEVICE] = remember }
    }
}
