package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class AccountValidationTest {
    @Test fun rejectsLoggedOut() {
        assertEquals("Log in before changing your password.", passwordValidationError(false, true, "oldpassword", "newpassword", "newpassword"))
    }

    @Test fun rejectsSocialAccount() {
        assertEquals("Password changes are unavailable for social accounts.", passwordValidationError(true, false, "oldpassword", "newpassword", "newpassword"))
    }

    @Test fun requiresCurrentPassword() {
        assertEquals("Enter your current password.", passwordValidationError(true, true, " ", "newpassword", "newpassword"))
    }

    @Test fun rejectsSevenCharacters() {
        assertEquals("The new password must contain at least 8 characters.", passwordValidationError(true, true, "oldpassword", "1234567", "1234567"))
    }

    @Test fun acceptsEightCharacters() {
        assertNull(passwordValidationError(true, true, "oldpassword", "12345678", "12345678"))
    }

    @Test fun rejectsMismatchedConfirmation() {
        assertEquals("The new passwords do not match.", passwordValidationError(true, true, "oldpassword", "newpassword", "different"))
    }

    @Test fun rejectsUnchangedPassword() {
        assertEquals("Choose a password different from the current one.", passwordValidationError(true, true, "oldpassword", "oldpassword", "oldpassword"))
    }

    @Test fun requiresLoggedInProfile() {
        assertEquals("Log in before changing account settings.", profileValidationError(false, "Ada", "Lovelace", true, "password1"))
    }

    @Test fun requiresFirstName() {
        assertEquals("First name is required.", profileValidationError(true, " ", "Lovelace", true, "password1"))
    }

    @Test fun requiresLastName() {
        assertEquals("Last name is required.", profileValidationError(true, "Ada", "", true, "password1"))
    }

    @Test fun rejectsInvalidEmailResult() {
        assertEquals("Enter a valid email address.", profileValidationError(true, "Ada", "Lovelace", false, "password1"))
    }

    @Test fun requiresVerificationPassword() {
        assertEquals("Enter your current password to save changes.", profileValidationError(true, "Ada", "Lovelace", true, " "))
    }

    @Test fun acceptsProfile() {
        assertNull(profileValidationError(true, "Ada", "Lovelace", true, "password1"))
    }

}
