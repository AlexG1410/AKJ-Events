package co.edu.uniquindio.akjevents.core.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Textos de `strings.xml` para los ViewModels y la capa de datos, que no tienen acceso a `stringResource`
 * (guía 14). En las pruebas se reemplaza por una versión que lee los textos en español.
 */
interface ResourceProvider {
    fun getString(@StringRes id: Int, vararg args: Any): String

    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String
}

class ResourceProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(id: Int, vararg args: Any): String = context.getString(id, *args)

    override fun getQuantityString(id: Int, quantity: Int, vararg args: Any): String =
        context.resources.getQuantityString(id, quantity, *args)
}
