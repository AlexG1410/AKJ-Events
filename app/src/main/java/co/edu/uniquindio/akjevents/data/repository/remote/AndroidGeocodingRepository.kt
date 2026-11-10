package co.edu.uniquindio.akjevents.data.repository.remote

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Usa el servicio del dispositivo, no la API de pago de Mapbox. Sin claves ni logs de coordenadas. */
class AndroidGeocodingRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : GeocodingRepository {
    override suspend fun reverseGeocode(location: Location): GeocodedAddress? {
        if (!Geocoder.isPresent()) throw IOException("Geocoder unavailable")
        val geocoder = Geocoder(context, Locale.getDefault())
        // El envoltorio permite distinguir una respuesta vacía de un timeout.
        val response = withTimeoutOrNull(10_000) {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(location.latitude, location.longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses.toList())
                        }

                        override fun onError(errorMessage: String?) {
                            // El mensaje del proveedor podría contener información de la consulta.
                            if (continuation.isActive) continuation.resumeWithException(IOException("Geocoder request failed"))
                        }
                    })
                }
            } else {
                // API 28–32: la llamada antigua puede bloquear; nunca ejecutarla en el hilo de UI.
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1).orEmpty()
                }
            }
            LookupResponse(addresses.firstOrNull()?.toGeocodedAddress())
        } ?: throw IOException("Geocoder timed out")
        return response.address
    }

    private data class LookupResponse(val address: GeocodedAddress?)
}

/** locality es el municipio; subAdminArea puede ser una provincia y no se presenta como ciudad. */
internal fun Address.toGeocodedAddress(): GeocodedAddress? {
    val municipality = locality?.trim().orEmpty()
    val region = adminArea?.trim().orEmpty()
    val city = if (municipality.isNotBlank()) {
        listOf(municipality, region).filter { it.isNotBlank() }.distinct().joinToString(", ")
    } else ""
    val line = getAddressLine(0)?.trim().orEmpty().ifBlank {
        listOfNotNull(thoroughfare, subThoroughfare, locality, adminArea, countryName)
            .map(String::trim).filter(String::isNotBlank).distinct().joinToString(", ")
    }
    return line.takeIf(String::isNotBlank)?.let { GeocodedAddress(it, city) }
}
