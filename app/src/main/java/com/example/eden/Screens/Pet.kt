package com.example.eden.Screens

data class Pet(
    val name: String,
    val species: String,
    val description: String,
    val objectId: String? = null,
    val record: Map<*, *> = emptyMap<Any, Any>()
)
