package co.edu.uniquindio.akjevents.core.component

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.theme.Terracotta

private val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

class LocationPermissionState(initial: Boolean) {
    var hasPermission by mutableStateOf(initial)
        internal set
    internal var request: (() -> Unit) -> Unit = {}
}

@Composable
fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    fun granted() = locationPermissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    val state = remember { LocationPermissionState(granted()) }
    var onGranted by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showExplanation by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        state.hasPermission = granted()
        if (state.hasPermission) onGranted?.invoke() else showExplanation = true
        onGranted = null
    }
    state.request = { callback ->
        state.hasPermission = granted()
        if (state.hasPermission) callback() else {
            onGranted = callback
            launcher.launch(locationPermissions)
        }
    }
    LifecycleResumeEffect(state) {
        state.hasPermission = granted()
        onPauseOrDispose { }
    }
    if (showExplanation) {
        AlertDialog(
            onDismissRequest = { showExplanation = false },
            title = { Text(stringResource(R.string.map_device_location)) },
            text = { Text(stringResource(R.string.map_location_permission_help)) },
            confirmButton = {
                TextButton(onClick = {
                    showExplanation = false
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                }) { Text(stringResource(R.string.map_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showExplanation = false }) { Text(stringResource(R.string.map_continue_manual)) }
            }
        )
    }
    return state
}

@Composable
fun BoxScope.LocationButton(permission: LocationPermissionState, onLocate: () -> Unit) {
    val currentOnLocate by rememberUpdatedState(onLocate)
    SmallFloatingActionButton(
        onClick = { permission.request { currentOnLocate() } },
        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        contentColor = Terracotta
    ) {
        Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.map_device_location))
    }
}
