package co.edu.uniquindio.akjevents.data.remote.ai

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/** URL pública del Worker, nunca una clave del proveedor. Rechaza HTTP y credenciales en la URL. */
class AiServiceConfig(baseUrl: String) {
    val baseUrl = baseUrl.trim().toHttpUrlOrNull()?.takeIf {
        it.isHttps && it.username.isEmpty() && it.password.isEmpty() &&
            it.query == null && it.fragment == null && it.encodedPath == "/"
    }
    val isConfigured: Boolean get() = baseUrl != null
}
