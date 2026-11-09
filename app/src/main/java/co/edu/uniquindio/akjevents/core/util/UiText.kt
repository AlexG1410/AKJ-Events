package co.edu.uniquindio.akjevents.core.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Texto de strings.xml con sus argumentos, para los mensajes que un UiState calcula por sí mismo
 * (por ejemplo, los errores de un campo) y que la pantalla traduce al mostrarlos.
 */
data class UiText(@param:StringRes val id: Int, val args: List<Any>) {
    constructor(@StringRes id: Int, vararg args: Any) : this(id, args.toList())

    @Composable
    fun asString(): String = stringResource(id, *args.toTypedArray())

    fun asString(resources: ResourceProvider): String = resources.getString(id, *args.toTypedArray())
}
