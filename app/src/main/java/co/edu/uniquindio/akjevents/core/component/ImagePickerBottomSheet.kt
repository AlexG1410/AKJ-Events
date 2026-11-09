package co.edu.uniquindio.akjevents.core.component

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import co.edu.uniquindio.akjevents.R
import co.edu.uniquindio.akjevents.core.theme.WarmBackground
import co.edu.uniquindio.akjevents.core.theme.WarmText
import co.edu.uniquindio.akjevents.core.util.createTempImageUri

/**
 * Panel inferior para elegir entre tomar una foto o elegirla de la galería (guía 15). Solo informa qué
 * opción eligió el usuario; los permisos y las apps de cámara y galería los maneja [rememberImagePicker].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerBottomSheet(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WarmBackground) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = stringResource(R.string.select_image),
                style = MaterialTheme.typography.titleMedium,
                color = WarmText,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            val colors = ListItemDefaults.colors(containerColor = WarmBackground)
            // Tomar una foto con la cámara (pide el permiso la primera vez)
            ListItem(
                headlineContent = { Text(stringResource(R.string.take_photo)) },
                leadingContent = { Icon(Icons.Outlined.PhotoCamera, contentDescription = null) },
                colors = colors,
                modifier = Modifier.clickable { onCameraClick() }
            )
            // Elegir una imagen de la galería (no necesita permiso)
            ListItem(
                headlineContent = { Text(stringResource(R.string.choose_from_gallery)) },
                leadingContent = { Icon(Icons.Outlined.Image, contentDescription = null) },
                colors = colors,
                modifier = Modifier.clickable { onGalleryClick() }
            )
        }
    }
}

/** Estado del selector de imagen: [open] lo muestra y [ImagePickerSheet] lo dibuja. */
@Stable
class ImagePickerState internal constructor() {
    internal var isVisible by mutableStateOf(false)
    internal var launchCamera: () -> Unit = {}
    internal var launchGallery: () -> Unit = {}

    fun open() {
        isVisible = true
    }
}

/**
 * Registra los tres launchers de la guía 15 (galería, cámara y permiso de cámara) y entrega la imagen
 * elegida a [onImagePicked]. Si el usuario niega el permiso se llama a [onCameraDenied]; la galería sigue
 * funcionando sin él.
 */
@Composable
fun rememberImagePicker(onImagePicked: (Uri) -> Unit, onCameraDenied: () -> Unit): ImagePickerState {
    val state = remember { ImagePickerState() }
    val context = LocalContext.current

    // Abre la galería; no requiere permiso
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(onImagePicked)
    }

    // URI donde la cámara guarda la foto. rememberSaveable porque Android puede recrear la actividad
    // mientras la cámara está abierta
    var tempCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) tempCameraUri?.let(onImagePicked)
    }

    // Pide el permiso de cámara; si ya estaba concedido, Android responde true sin mostrar el diálogo
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
        if (granted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            onCameraDenied()
        }
    }

    state.launchCamera = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    state.launchGallery = { galleryLauncher.launch("image/*") }
    return state
}

/** Muestra el panel de [ImagePickerBottomSheet] mientras [state] esté abierto. */
@Composable
fun ImagePickerSheet(state: ImagePickerState) {
    if (!state.isVisible) return
    ImagePickerBottomSheet(
        onCameraClick = {
            state.isVisible = false
            state.launchCamera()
        },
        onGalleryClick = {
            state.isVisible = false
            state.launchGallery()
        },
        onDismiss = { state.isVisible = false }
    )
}
