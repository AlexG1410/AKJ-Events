package co.edu.uniquindio.akjevents.core.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit, // Se avisa que el usuario confirmó
    onDismiss: () -> Unit, // Se avisa que el usuario canceló o cerró el diálogo
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar"
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = message) },
        // Se ejecuta al tocar fuera del diálogo o presionar el botón "atrás"
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        }
    )
}