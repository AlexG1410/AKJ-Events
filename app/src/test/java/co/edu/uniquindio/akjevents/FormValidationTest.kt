package co.edu.uniquindio.akjevents

import co.edu.uniquindio.akjevents.core.util.FormValidation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidationTest {
    @Test
    fun validEmailRequiresACompleteDomain() {
        assertTrue(FormValidation.isValidEmail("persona@ejemplo.com"))
        assertFalse(FormValidation.isValidEmail("persona@ejemplo"))
        assertFalse(FormValidation.isValidEmail("persona-ejemplo.com"))
    }

    @Test
    fun strongPasswordRequiresLengthUppercaseAndNumber() {
        assertTrue(FormValidation.isStrongPassword("Comunidad1"))
        assertFalse(FormValidation.isStrongPassword("comunidad1"))
        assertFalse(FormValidation.isStrongPassword("Comunidad"))
        assertFalse(FormValidation.isStrongPassword("Corta1"))
    }
}
