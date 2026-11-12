package com.example.eden.Screens

data class Pet(
    val name: String,
    val species: String,
    val breed: String,
    val gender: String?,
    val image: String,
    val status: String,
    val ageMonths: Double,
    val description: String,
    val objectId: String? = null,
    val record: Map<*, *> = emptyMap<Any, Any>()
)
