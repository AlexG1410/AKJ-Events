package co.edu.uniquindio.akjevents.features.checkin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import co.edu.uniquindio.akjevents.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** ZXing Embedded: cámara y decodificador offline, sin descargar modelos ni Google Play Services. */
@Composable
internal fun rememberQrScanner(onCode: (String?) -> Unit, onMessage: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val codeCallback by rememberUpdatedState(onCode)
    val messageCallback by rememberUpdatedState(onMessage)
    var active by rememberSaveable { mutableStateOf(false) }
    val prompt = stringResource(R.string.qr_camera_prompt)
    val denied = stringResource(R.string.qr_camera_denied)
    val unavailable = stringResource(R.string.qr_scanner_unavailable)
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        active = false
        codeCallback(result.contents)
    }
    val launch = {
        try {
            scanner.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setBeepEnabled(false).setOrientationLocked(false).setPrompt(prompt))
        } catch (_: Exception) { active = false; messageCallback(unavailable) }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launch() else { active = false; messageCallback(denied) }
    }
    return {
        if (!active) {
            active = true
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launch()
            else permission.launch(Manifest.permission.CAMERA)
        }
    }
}

internal fun android.content.Context.openQrSettings() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
}
