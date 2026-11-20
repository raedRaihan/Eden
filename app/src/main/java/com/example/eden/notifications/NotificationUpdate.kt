package com.example.eden.notifications

internal fun notificationUpdate(objectId: String?, markRead: Boolean): Map<Any?, Any?> =
    mapOf(
        "objectId" to objectId,
        (if (markRead) "read" else "active") to markRead
    )
