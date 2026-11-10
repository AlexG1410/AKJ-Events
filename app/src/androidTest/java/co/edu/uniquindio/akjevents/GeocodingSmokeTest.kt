package co.edu.uniquindio.akjevents

import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import co.edu.uniquindio.akjevents.core.theme.AKJEventsTheme
import co.edu.uniquindio.akjevents.core.util.PickedLocation
import co.edu.uniquindio.akjevents.core.util.ResourceProviderImpl
import co.edu.uniquindio.akjevents.data.repository.remote.AndroidGeocodingRepository
import co.edu.uniquindio.akjevents.data.repository.remote.toGeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.GeocodedAddress
import co.edu.uniquindio.akjevents.domain.model.Location
import co.edu.uniquindio.akjevents.domain.repository.GeocodingRepository
import co.edu.uniquindio.akjevents.domain.usecase.location.ReverseGeocodeUseCase
import co.edu.uniquindio.akjevents.features.map.pick.AddressLookupState
import co.edu.uniquindio.akjevents.features.map.pick.PickLocationScreen
import co.edu.uniquindio.akjevents.features.map.pick.PickLocationViewModel
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Aisladas de Firebase/Cloudinary: solo el test del proveedor consulta el servicio del dispositivo. */
class GeocodingSmokeTest {
    @get:Rule val compose = createComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val initial = PickedLocation("Dirección anterior", Location(4.5339, -75.6811), "Armenia, Quindío")
    private fun text(id: Int) = context.getString(id)
    private val resources = ResourceProviderImpl(context)

    @Test fun foundAddressCanBeConfirmedWithItsRealCity() {
        val chosen = AtomicReference<PickedLocation?>(null)
        lateinit var vm: PickLocationViewModel
        compose.runOnUiThread {
            vm = PickLocationViewModel(initial, resources, ReverseGeocodeUseCase(object : GeocodingRepository {
                override suspend fun reverseGeocode(location: Location) = GeocodedAddress("Calle de prueba 10", "Ciudad de prueba, Región")
            }))
        }
        compose.setContent {
            AKJEventsTheme { PickLocationScreen(initial, {}, { chosen.set(it) }, vm) }
        }
        compose.onNodeWithText(text(R.string.pick_lookup_address)).performClick()
        compose.waitUntil(10_000) { vm.uiState.value.addressLookup == AddressLookupState.FOUND }
        compose.onNodeWithText("Calle de prueba 10").assertExists()
        compose.onNodeWithText("Ciudad de prueba, Región").assertExists()
        // Solo una captura de datos de prueba, para revisar que el mapa y la tarjeta caben en pantalla.
        val outputDir = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let(::File)
            ?: context.externalCacheDir
        File(outputDir, "picker-geocoded.png").outputStream().use {
            instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        compose.onNodeWithText(text(R.string.pick_confirm)).performClick()
        assertEquals(PickedLocation("Calle de prueba 10", initial.location, "Ciudad de prueba, Región"), chosen.get())
    }

    @Test fun manualAddressCanReplacePendingLookupWithoutMovingCoordinates() {
        val pending = CompletableDeferred<GeocodedAddress?>()
        val chosen = AtomicReference<PickedLocation?>(null)
        lateinit var vm: PickLocationViewModel
        compose.runOnUiThread {
            vm = PickLocationViewModel(initial, resources, ReverseGeocodeUseCase(object : GeocodingRepository {
                override suspend fun reverseGeocode(location: Location) = pending.await()
            }))
        }
        compose.setContent { AKJEventsTheme { PickLocationScreen(initial, {}, { chosen.set(it) }, vm) } }
        compose.onNodeWithText(text(R.string.pick_lookup_address)).performClick()
        compose.onNodeWithText(text(R.string.pick_confirm)).assertIsNotEnabled()
        compose.onNodeWithText(text(R.string.pick_edit_address)).performClick()
        compose.onNodeWithContentDescription(text(R.string.pick_address_label)).performTextReplacement("Entrada norte")
        compose.onNodeWithContentDescription(text(R.string.pick_city_label)).performTextReplacement("Mi ciudad")
        compose.onNodeWithText(text(R.string.pick_save_address)).performClick()
        compose.onNodeWithText(text(R.string.pick_confirm)).performClick()
        assertEquals(PickedLocation("Entrada norte", initial.location, "Mi ciudad"), chosen.get())
        pending.complete(GeocodedAddress("Tardía", "Otra ciudad"))
        compose.waitForIdle()
        assertEquals("Entrada norte", vm.result().address)
    }

    @Test fun androidAddressMappingDoesNotInventCityFromProvince() {
        val address = Address(Locale.forLanguageTag("es-CO"))
        address.setAddressLine(0, "Calle 10 # 2, Armenia, Colombia")
        address.locality = "Armenia"
        address.adminArea = "Quindío"
        assertEquals(GeocodedAddress("Calle 10 # 2, Armenia, Colombia", "Armenia, Quindío"), address.toGeocodedAddress())
        address.locality = null
        address.subAdminArea = "Provincia"
        assertEquals("", address.toGeocodedAddress()?.city)
        assertNull(Address(Locale.US).toGeocodedAddress())
    }

    @Test fun deviceGeocoderCanResolveAPublicLandmarkWithoutApiKey() = runBlocking {
        assumeTrue("This device has no Geocoder backend", Geocoder.isPresent())
        val resolved = AndroidGeocodingRepository(context).reverseGeocode(initial.location)
        assertNotNull("No address returned by device provider", resolved)
        assertTrue(resolved!!.address.isNotBlank())
        // No se exige calle exacta: la cobertura y el detalle dependen del proveedor.
    }
}
