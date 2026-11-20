package com.example.eden.Screens

import org.junit.Assert.assertEquals
import org.junit.Test

class AdminUserTest {
    private fun user(first: String, last: String) =
        AdminUser("user", "ada@example.com", first, last, "", "Adopter", "ENABLED")

    @Test fun combinesBothNames() {
        assertEquals("Ada Lovelace", user("Ada", "Lovelace").displayName)
    }

    @Test fun missingFirstNameDoesNotLeaveLeadingSeparator() {
        assertEquals("Lovelace", user(" ", "Lovelace").displayName)
    }

    @Test fun missingLastNameDoesNotLeaveTrailingSeparator() {
        assertEquals("Ada", user("Ada", "").displayName)
    }

    @Test fun missingNamesAreEmpty() {
        assertEquals("", user("", " ").displayName)
    }
}
