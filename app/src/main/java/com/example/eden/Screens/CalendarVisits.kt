package com.example.eden.Screens

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class CalendarVisits(
    val upcoming: List<CalendarVisit>,
    val past: List<CalendarVisit>
)

internal fun calendarVisitDay(visit: CalendarVisit, zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(visit.timeMillis).atZone(zone).toLocalDate()

internal fun groupCalendarVisits(
    visits: List<CalendarVisit>,
    selectedDay: LocalDate?,
    zone: ZoneId,
    nowMillis: Long
): CalendarVisits {
    val visible = visits.filter { selectedDay == null || calendarVisitDay(it, zone) == selectedDay }
    return CalendarVisits(
        upcoming = visible.filter { it.timeMillis >= nowMillis }.sortedBy { it.timeMillis },
        past = visible.filter { it.timeMillis < nowMillis }.sortedByDescending { it.timeMillis }
    )
}
