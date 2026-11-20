package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class PreferencesValidationTest {
    @Test fun allowsSkippingAges() {
        assertNull(preferencesValidationError("", "", true))
    }

    @Test fun allowsMinimumOnly() {
        assertNull(preferencesValidationError("12", "", true))
    }

    @Test fun allowsMaximumOnly() {
        assertNull(preferencesValidationError("", "12", true))
    }

    @Test fun allowsEqualBounds() {
        assertNull(preferencesValidationError("12", "12", true))
    }

    @Test fun allowsZero() {
        assertNull(preferencesValidationError("0", "0", true))
    }

    @Test fun rejectsDecimalMinimum() {
        assertEquals("Minimum age must be a whole number.", preferencesValidationError("1.5", "12", true))
    }

    @Test fun rejectsTextMaximum() {
        assertEquals("Maximum age must be a whole number.", preferencesValidationError("0", "abc", true))
    }

    @Test fun rejectsMinimumOverflow() {
        assertEquals("Minimum age must be a whole number.", preferencesValidationError("2147483648", "", true))
    }

    @Test fun rejectsNegativeMinimum() {
        assertEquals("Minimum age cannot be negative.", preferencesValidationError("-1", "12", true))
    }

    @Test fun rejectsNegativeMaximum() {
        assertEquals("Maximum age cannot be negative.", preferencesValidationError("0", "-1", true))
    }

    @Test fun rejectsReversedBounds() {
        assertEquals("Minimum age cannot be greater than maximum age.", preferencesValidationError("13", "12", true))
    }

    @Test fun requiresLogin() {
        assertEquals("Log in before saving preferences.", preferencesValidationError("", "", false))
    }

}
