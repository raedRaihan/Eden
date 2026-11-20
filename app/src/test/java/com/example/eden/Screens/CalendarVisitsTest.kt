package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarVisitsTest {
    private val zone = ZoneId.of("America/Chicago")
    private val now = Instant.parse("2026-11-17T18:00:00Z").toEpochMilli()
    private fun visit(time: Long, name: String = "Bella") = CalendarVisit(time, name, "PENDING")

    @Test fun separatesAndOrdersPastAndUpcoming() {
        val result = groupCalendarVisits(
            listOf(visit(now + 20), visit(now - 20), visit(now + 10), visit(now - 10)),
            null, zone, now
        )
        assertEquals(listOf(now + 10, now + 20), result.upcoming.map { it.timeMillis })
        assertEquals(listOf(now - 10, now - 20), result.past.map { it.timeMillis })
    }

    @Test fun appointmentAtCurrentTimeIsUpcoming() {
        val result = groupCalendarVisits(listOf(visit(now)), null, zone, now)
        assertEquals(1, result.upcoming.size)
        assertTrue(result.past.isEmpty())
    }

    @Test fun filtersByLocalDayRatherThanUtcDay() {
        val appointment = visit(Instant.parse("2026-11-18T02:00:00Z").toEpochMilli())
        assertEquals(LocalDate.of(2026, 11, 17), calendarVisitDay(appointment, zone))
        assertEquals(1, groupCalendarVisits(listOf(appointment), LocalDate.of(2026, 11, 17), zone, now).upcoming.size)
        assertTrue(groupCalendarVisits(listOf(appointment), LocalDate.of(2026, 11, 18), zone, now).upcoming.isEmpty())
    }

    @Test fun preservesBothVisitsDuringDaylightSavingRepeatedHour() {
        val visits = listOf(
            visit(Instant.parse("2026-11-01T06:30:00Z").toEpochMilli(), "Bella"),
            visit(Instant.parse("2026-11-01T07:30:00Z").toEpochMilli(), "Bear")
        )
        val result = groupCalendarVisits(visits, LocalDate.of(2026, 11, 1), zone, now)
        assertEquals(listOf("Bear", "Bella"), result.past.map { it.petName })
    }

    @Test fun handlesEmptyCalendar() {
        val result = groupCalendarVisits(emptyList(), null, zone, now)
        assertTrue(result.upcoming.isEmpty())
        assertTrue(result.past.isEmpty())
    }

    @Test fun keepsMultipleVisitsOnSameDay() {
        val result = groupCalendarVisits(listOf(visit(now), visit(now + 1000)), LocalDate.of(2026, 11, 17), zone, now)
        assertEquals(2, result.upcoming.size)
    }
}
