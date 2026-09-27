package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class) // Algunos composables aún están en fase experimental
@Composable
fun DropdownMenu(
    value: String,
    label: String,
    supportingText: String? = null,
    list: List<String>,
    icon: ImageVector? = null,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {

    // Estado interno para controlar si el menú está expandido o no
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { expanded = !expanded }
    ) {

        OutlinedTextField(
            enabled = enabled,
            readOnly = true,
            value = value, // Muestra el valor seleccionado
            onValueChange = { },
            label = {
                Text(text = label) // Muestra la etiqueta del campo
            },
            supportingText = supportingText?.let {
                { Text(text = supportingText) } // Muestra el texto de soporte si se proporciona
            },
            leadingIcon = icon?.let {
                { Icon(imageVector = icon, contentDescription = null) } // Muestra el icono si se proporciona
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) }, // Icono de flecha para el menú desplegable
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            // Genera un elemento del menú para cada opción en la lista
            list.forEach {
                DropdownMenuItem(
                    text = {
                        Text(text = it)
                    },
                    onClick = {
                        onValueChange(it) // Actualiza el valor seleccionado al hacer clic
                        expanded = false // Cierra el menú desplegable
                    }
                )
            }
        }

    }
}