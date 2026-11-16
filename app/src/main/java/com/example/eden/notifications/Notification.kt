package com.example.eden.notifications



data class Notification(
    var active: Boolean = true,
    var message: String? = "",
    var read: Boolean = false,
    val objectId: String? = null
)
