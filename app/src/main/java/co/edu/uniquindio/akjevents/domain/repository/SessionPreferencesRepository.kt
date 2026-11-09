package co.edu.uniquindio.akjevents.domain.repository

import kotlinx.coroutines.flow.Flow

/** Preferencias de la sesión que se guardan en el teléfono (guía 13). */
interface SessionPreferencesRepository {
    /** Si es false, la sesión se cierra la próxima vez que se abra la app. */
    val rememberDevice: Flow<Boolean>

    suspend fun setRememberDevice(remember: Boolean)
}
