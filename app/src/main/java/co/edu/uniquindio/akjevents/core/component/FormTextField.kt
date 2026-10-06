package co.edu.uniquindio.akjevents.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.edu.uniquindio.akjevents.core.theme.BrandBorder
import co.edu.uniquindio.akjevents.core.theme.BrandDark
import co.edu.uniquindio.akjevents.core.theme.BrandFieldFill
import co.edu.uniquindio.akjevents.core.theme.BrandMuted
import co.edu.uniquindio.akjevents.core.theme.BrandTerracotta

/** Apariencia de un [FormTextField]; cada mockup define la suya. */
data class FormFieldStyle(
    val height: Dp,
    val shape: Shape,
    val containerColor: Color,
    val focusedContainerColor: Color = containerColor,
    val borderColor: Color,
    val focusedBorderColor: Color,
    val focusedBorderWidth: Dp = 1.dp,
    val textStyle: TextStyle,
    val placeholderColor: Color,
    val iconColor: Color,
    val iconSize: Dp,
    val iconStartPadding: Dp,
    val textStartPadding: Dp
)

object FormFieldStyles {
    /** Campos de 02_login y 03_registro. */
    val Brand = FormFieldStyle(
        height = 44.dp,
        shape = RoundedCornerShape(16.dp),
        containerColor = BrandFieldFill,
        borderColor = BrandBorder,
        focusedBorderColor = BrandTerracotta,
        textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandDark),
        placeholderColor = BrandMuted,
        iconColor = BrandMuted,
        iconSize = 18.dp,
        iconStartPadding = 14.dp,
        textStartPadding = 40.dp
    )
}

/** Campo de una línea con ícono inicial y contenido final opcionales, como en los mockups. */
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector?,
    modifier: Modifier = Modifier,
    style: FormFieldStyle = FormFieldStyles.Brand,
    leadingIconTint: Color = style.iconColor,
    trailingContent: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(style.height),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = style.textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(style.focusedBorderColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFocused) style.focusedContainerColor else style.containerColor, style.shape)
                    .border(
                        width = if (isFocused) style.focusedBorderWidth else 1.dp,
                        color = if (isFocused) style.focusedBorderColor else style.borderColor,
                        shape = style.shape
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = leadingIconTint,
                        modifier = Modifier.padding(start = style.iconStartPadding).size(style.iconSize)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = style.textStartPadding, end = if (trailingContent != null) 44.dp else 16.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = style.textStyle.copy(color = style.placeholderColor, fontWeight = FontWeight.Normal))
                    }
                    innerTextField()
                }
                if (trailingContent != null) {
                    Row(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)) { trailingContent() }
                }
            }
        }
    )
}

/** Campo de contraseña con botón para mostrarla u ocultarla. */
@Composable
fun PasswordFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    style: FormFieldStyle = FormFieldStyles.Brand,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        modifier = modifier,
        style = style,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingContent = {
            IconButton(onClick = onTogglePasswordVisibility, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = style.iconColor,
                    modifier = Modifier.size(style.iconSize)
                )
            }
        }
    )
}
