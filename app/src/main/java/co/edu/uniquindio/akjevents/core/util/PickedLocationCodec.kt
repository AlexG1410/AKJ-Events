package co.edu.uniquindio.akjevents.core.util

import co.edu.uniquindio.akjevents.domain.model.Location
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Resultado simple para SavedStateHandle; JSON conserva direcciones/ciudades con separadores. */
object PickedLocationCodec {
    @Serializable
    private data class Payload(val latitude: Double, val longitude: Double, val address: String, val city: String? = null)

    fun encode(picked: PickedLocation): String = Json.encodeToString(
        Payload(picked.location.latitude, picked.location.longitude, picked.address, picked.city)
    )

    fun decode(encoded: String): PickedLocation? {
        if (encoded.startsWith("{")) {
            val payload = runCatching { Json.decodeFromString<Payload>(encoded) }.getOrNull() ?: return null
            return validLocation(payload.latitude, payload.longitude)?.let { PickedLocation(payload.address, it, payload.city) }
        }
        // Compatibilidad con resultados guardados antes de añadir la ciudad al selector.
        val parts = encoded.split("|", limit = 3)
        val latitude = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
        val longitude = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
        return validLocation(latitude, longitude)?.let { PickedLocation(parts.getOrNull(2).orEmpty(), it) }
    }

    private fun validLocation(latitude: Double, longitude: Double): Location? =
        if (latitude.isFinite() && longitude.isFinite() && latitude in -90.0..90.0 && longitude in -180.0..180.0) Location(latitude, longitude) else null
}
