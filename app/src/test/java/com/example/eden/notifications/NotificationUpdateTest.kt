package com.example.eden.notifications

import org.junit.Assert.*
import org.junit.Test

class NotificationUpdateTest {
    @Test fun markingReadDoesNotDeactivateNotification() {
        val update = notificationUpdate("notification-id", true)
        assertEquals("notification-id", update["objectId"])
        assertEquals(true, update["read"])
        assertFalse(update.containsKey("active"))
        assertFalse(update.containsKey("message"))
    }

    @Test fun dismissingNotificationDoesNotChangeReadState() {
        val update = notificationUpdate("notification-id", false)
        assertEquals("notification-id", update["objectId"])
        assertEquals(false, update["active"])
        assertFalse(update.containsKey("read"))
    }
}
