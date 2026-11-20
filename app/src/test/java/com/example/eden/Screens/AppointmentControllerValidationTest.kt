package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class AppointmentControllerValidationTest {
    private val controller = AppointmentController()

    @Test fun invalidAdoptionReportsErrorWithoutSuccess() {
        var error: String? = null
        controller.submitAdoption(
            requestId = "request",
            petId = "pet",
            userId = "user",
            housingType = "House",
            experienceLevel = "Experienced",
            hasOtherPets = false,
            hoursAloneRange = "0-4 hours",
            adoptionReason = "   ",
            responsibilityAccepted = true,
            notes = "",
            onSuccess = { fail("Blank applications must not be saved.") },
            onError = { error = it }
        )
        assertEquals(
            "Explain why you want to adopt this pet in 250 characters or fewer.",
            error
        )
    }

    @Test fun adoptionWithoutUserIsBlocked() {
        var error: String? = null
        controller.submitAdoption(
            requestId = "request",
            petId = "pet",
            userId = "",
            housingType = "House",
            experienceLevel = "Experienced",
            hasOtherPets = false,
            hoursAloneRange = "0-4 hours",
            adoptionReason = "My application",
            responsibilityAccepted = true,
            notes = "",
            onSuccess = { fail("Anonymous applications must not be saved.") },
            onError = { error = it }
        )
        assertEquals("Sign in and select a pet before applying.", error)
    }

    @Test fun pastVisitIsBlockedBeforeDatabaseRequest() {
        var error: String? = null
        controller.submitVisit(
            "request", "pet", "user", 0L, "",
            onSuccess = { fail("Past visits must not be saved.") },
            onError = { error = it }
        )
        assertEquals("Sign in, select a real pet, and choose a future visit time.", error)
    }

    @Test fun missingPetBlocksVisitationLookup() {
        var error: String? = null
        controller.findVisitAppointment(
            "user", "",
            onSuccess = { fail("Invalid lookup must not report success.") },
            onError = { error = it }
        )
        assertNotNull(error)
    }

    @Test fun missingUserBlocksAdoptionLookup() {
        var error: String? = null
        controller.findAdoptionApplication(
            "", "pet",
            onSuccess = { fail("Invalid lookup must not report success.") },
            onError = { error = it }
        )
        assertEquals("Sign in and select a pet first.", error)
    }

    @Test fun missingIdBlocksCancellation() {
        var error: String? = null
        controller.cancelVisit(
            " ",
            onSuccess = { fail("Cancellation needs an appointment ID.") },
            onError = { error = it }
        )
        assertEquals("Appointment ID is missing.", error)
    }

    @Test fun calendarWithoutUserReportsError() {
        var failed = false
        controller.loadCalendarVisits(
            "",
            onSuccess = { fail("Calendar must not query all users.") },
            onError = { failed = true }
        )
        assertTrue(failed)
    }
}
