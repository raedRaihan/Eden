package com.example.eden.Screens

import java.util.Date

data class Appointment(
    val objectId: String,
    val dateAndTime: Date,
    val requestStatus: String,
    val notes: String
)
