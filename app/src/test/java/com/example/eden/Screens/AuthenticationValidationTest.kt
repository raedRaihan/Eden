package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class AuthenticationValidationTest {
    @Test fun rejectsEmptyFirstName() {
        assertEquals("All fields are required.", signUpValidationError("", "Lovelace", "ada@example.com", "password1", "password1"))
    }

    @Test fun rejectsWhitespaceFirstName() {
        assertEquals("All fields are required.", signUpValidationError("   ", "Lovelace", "ada@example.com", "password1", "password1"))
    }

    @Test fun rejectsEmptyLastName() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "", "ada@example.com", "password1", "password1"))
    }

    @Test fun rejectsWhitespaceLastName() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "   ", "ada@example.com", "password1", "password1"))
    }

    @Test fun rejectsEmptyEmail() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "", "password1", "password1"))
    }

    @Test fun rejectsWhitespaceEmail() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "   ", "password1", "password1"))
    }

    @Test fun rejectsEmptyPassword() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "ada@example.com", "", "password1"))
    }

    @Test fun rejectsWhitespacePassword() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "ada@example.com", "   ", "password1"))
    }

    @Test fun rejectsEmptyConfirmation() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "ada@example.com", "password1", ""))
    }

    @Test fun rejectsWhitespaceConfirmation() {
        assertEquals("All fields are required.", signUpValidationError("Ada", "Lovelace", "ada@example.com", "password1", "   "))
    }

    @Test fun rejectsEmailWithoutAt() {
        assertEquals("Enter a valid email address.", signUpValidationError("Ada", "Lovelace", "invalid", "password1", "password1"))
    }

    @Test fun rejectsMismatchedPasswords() {
        assertEquals("Passwords do not match.", signUpValidationError("Ada", "Lovelace", "ada@example.com", "password1", "password2"))
    }

    @Test fun acceptsValidSignUp() {
        assertNull(signUpValidationError("Ada", "Lovelace", "ada@example.com", "password1", "password1"))
    }

    @Test fun enablesLoginWithBothFields() {
        assertTrue(canSubmitLogin("ada@example.com", "password1", false))
    }

    @Test fun disablesLoginWhileLoading() {
        assertFalse(canSubmitLogin("ada@example.com", "password1", true))
    }

    @Test fun disablesLoginWithoutEmail() {
        assertFalse(canSubmitLogin("", "password1", false))
    }

    @Test fun disablesLoginWithoutPassword() {
        assertFalse(canSubmitLogin("ada@example.com", "", false))
    }

    @Test fun disablesLoginWithWhitespace() {
        assertFalse(canSubmitLogin("  ", "   ", false))
    }

}
