package co.edu.uniquindio.akjevents.data.repository

import co.edu.uniquindio.akjevents.domain.repository.SessionPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Preferencias de sesión en memoria, para las pruebas. */
class InMemorySessionPreferences(rememberDevice: Boolean = false) : SessionPreferencesRepository {
    private val _rememberDevice = MutableStateFlow(rememberDevice)
    override val rememberDevice: Flow<Boolean> = _rememberDevice

    override suspend fun setRememberDevice(remember: Boolean) {
        _rememberDevice.value = remember
    }
}
