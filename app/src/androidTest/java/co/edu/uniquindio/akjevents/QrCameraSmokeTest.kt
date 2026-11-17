package co.edu.uniquindio.akjevents

import android.Manifest
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import co.edu.uniquindio.akjevents.features.checkin.rememberQrScanner
import org.junit.Rule
import org.junit.Test

/** Abre la cámara real en emulador, vuelve con Atrás y no registra asistencia. */
class QrCameraSmokeTest {
    @get:Rule val compose = createComposeRule()
    @Test fun cameraActivityOpensAndBackCancelsWithoutAScannedCode() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.CAMERA)
        compose.setContent {
            var outcome by remember { mutableStateOf("waiting") }
            val scan = rememberQrScanner({ outcome = if (it == null) "cancelled" else "code" }, { outcome = "error" })
            Button(onClick = scan) { Text("Open test camera") }
            Text(outcome)
        }
        compose.onNodeWithText("Open test camera").performClick()
        compose.waitUntil(10_000) {
            var capture = false
            instrumentation.runOnMainSync { capture = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any { it.javaClass.name.contains("CaptureActivity") } }
            capture
        }
        Espresso.pressBack()
        compose.onNodeWithText("cancelled").assertExists()
    }
}
