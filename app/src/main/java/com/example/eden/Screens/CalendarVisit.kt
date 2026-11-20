package com.example.eden.Screens

internal data class CalendarVisit(
    val timeMillis: Long,
    val petId: String,
    val petName: String?,
    val petStatus: String,
    val status: String,
    val appointmentType: String = "VISITATION",
    val adopterName: String = ""
)
