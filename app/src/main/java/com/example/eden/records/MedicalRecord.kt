package com.example.eden.records

import java.time.LocalDate

data class MedicalRecord(
    val dateFilledOut: LocalDate,
    val medications: String, // separated by comas
    val vaccines: String, // separated by comas
    val veterinarian: String,
    val objectId: String? = null
)
