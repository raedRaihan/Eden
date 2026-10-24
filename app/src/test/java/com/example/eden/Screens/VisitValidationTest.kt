package com.example.eden.Screens

import com.example.eden.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VisitValidationTest {
    @Test fun requiresExplicitDateSelection() {
        assertEquals(R.string.visit_date_required, visitValidationError(false, true, 2000, 1000))
    }

    @Test fun requiresExplicitTimeSelection() {
        assertEquals(R.string.visit_time_required, visitValidationError(true, false, 2000, 1000))
    }

    @Test fun rejectsPastVisit() {
        assertEquals(R.string.visit_future_required, visitValidationError(true, true, 999, 1000))
    }

    @Test fun rejectsCurrentTime() {
        assertEquals(R.string.visit_future_required, visitValidationError(true, true, 1000, 1000))
    }

    @Test fun acceptsFutureVisit() {
        assertNull(visitValidationError(true, true, 2000, 1000))
    }
}
