package co.edu.uniquindio.akjevents.data.demo

/** Credenciales locales para demostrar el inicio de sesión mientras no exista un backend. */
object DemoCredentials {
    val email: String get() = SampleUsers.demoUser.email
    const val password: String = "AkjEvents123"

    /** Cuenta de moderador precargada. */
    val moderatorEmail: String get() = SampleUsers.moderators.first().email
    const val moderatorPassword: String = "Moderador123"
}
