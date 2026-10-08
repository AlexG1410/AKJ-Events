package co.edu.uniquindio.akjevents.core.util

/** Reglas de validación compartidas por los formularios de autenticación. */
object FormValidation {
    const val MIN_PASSWORD_LENGTH = 8

    private val emailRegex = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    fun isStrongPassword(password: String): Boolean =
        isValidPassword(password) && password.any(Char::isUpperCase) && password.any(Char::isDigit)

    const val INVALID_EMAIL_MESSAGE = "Ingresa un correo electrónico válido"
    const val SHORT_PASSWORD_MESSAGE = "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
    const val WEAK_PASSWORD_MESSAGE =
        "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres, una mayúscula y un número"
}
