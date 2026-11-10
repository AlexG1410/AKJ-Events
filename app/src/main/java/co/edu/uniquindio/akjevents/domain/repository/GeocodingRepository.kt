package co.edu.uniquindio.akjevents.domain.repository

import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location

interface GeocodingRepository {
    /** null si no hay dirección. Puede fallar si el proveedor o la red no están disponibles. */
    suspend fun reverseGeocode(location: Location): GeocodedAddress?
}
