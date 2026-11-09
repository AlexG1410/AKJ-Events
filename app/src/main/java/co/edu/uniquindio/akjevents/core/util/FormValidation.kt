package co.edu.uniquindio.akjevents.core.util

import co.edu.uniquindio.akjevents.R

/** Reglas de validación compartidas por los formularios de autenticación. */
object FormValidation {
    const val MIN_PASSWORD_LENGTH = 8

    private val emailRegex = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    fun isStrongPassword(password: String): Boolean =
        isValidPassword(password) && password.any(Char::isUpperCase) && password.any(Char::isDigit)

    // Mensajes en strings.xml; los de contraseña reciben MIN_PASSWORD_LENGTH como argumento
    val INVALID_EMAIL_MESSAGE = UiText(R.string.error_invalid_email)
    val SHORT_PASSWORD_MESSAGE = UiText(R.string.error_short_password, MIN_PASSWORD_LENGTH)
    val WEAK_PASSWORD_MESSAGE = UiText(R.string.error_weak_password, MIN_PASSWORD_LENGTH)
}
