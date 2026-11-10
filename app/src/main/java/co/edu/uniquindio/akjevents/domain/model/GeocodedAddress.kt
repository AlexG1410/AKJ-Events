package co.edu.uniquindio.akjevents.domain.model

/** Texto devuelto por el proveedor; no modifica las coordenadas elegidas por la persona. */
data class GeocodedAddress(val address: String, val city: String = "")
