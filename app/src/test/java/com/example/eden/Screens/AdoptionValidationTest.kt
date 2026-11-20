package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class AdoptionValidationTest {
    @Test fun requiresRequestId() {
        assertEquals("Sign in and select a pet before applying.", adoptionSubmissionError(" ", "pet", "user", "My application"))
    }

    @Test fun requiresPetId() {
        assertEquals("Sign in and select a pet before applying.", adoptionSubmissionError("request", " ", "user", "My application"))
    }

    @Test fun requiresUserId() {
        assertEquals("Sign in and select a pet before applying.", adoptionSubmissionError("request", "pet", " ", "My application"))
    }

    @Test fun rejectsEmpty() {
        assertEquals("Write an application between 1 and 500 characters.", adoptionSubmissionError("request", "pet", "user", ""))
    }

    @Test fun rejectsWhitespace() {
        assertEquals("Write an application between 1 and 500 characters.", adoptionSubmissionError("request", "pet", "user", "   "))
    }

    @Test fun acceptsOneCharacter() {
        assertNull(adoptionSubmissionError("request", "pet", "user", "a"))
    }

    @Test fun accepts500Characters() {
        assertNull(adoptionSubmissionError("request", "pet", "user", "a".repeat(500)))
    }

    @Test fun rejects501Characters() {
        assertEquals("Write an application between 1 and 500 characters.", adoptionSubmissionError("request", "pet", "user", "a".repeat(501)))
    }

    @Test fun trimsBeforeCounting() {
        assertNull(adoptionSubmissionError("request", "pet", "user", "  " + "a".repeat(500) + "  "))
    }

}
