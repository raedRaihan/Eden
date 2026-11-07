package com.example.eden.notifications



data class notification(
    val active: String, // separated by comas
    val message: String, // separated by comas
    val read: String,
    val userId: String,
    val objectId: String? = null
)
