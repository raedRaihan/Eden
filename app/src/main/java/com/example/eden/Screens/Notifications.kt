package com.example.eden.Screens

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.R
import com.example.eden.notifications.notificationController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import com.google.android.material.R as MaterialR
import java.text.DateFormat
import java.util.Date

@Composable
fun Notifications(navController: NavController, email: String, isAdmin: Boolean) {
    var notifications by remember(email) {
        mutableStateOf<List<Map<*, *>>>(emptyList())
    }
    var refreshKey by remember(email) { mutableIntStateOf(0) }

    LaunchedEffect(email, refreshKey) {
        if (email.isBlank()) {
            notifications = emptyList()
            return@LaunchedEffect
        }

        notificationController().searchForNotifications(
            email = email,
            onSuccess = { response -> notifications = response },
            onError = { message ->
                Log.e("Notifications", "Search failure: $message")
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            factory = { context ->
                val themedContext = ContextThemeWrapper(
                    context,
                    MaterialR.style.Theme_MaterialComponents_DayNight_NoActionBar,
                )
                LayoutInflater.from(themedContext).inflate(
                    R.layout.notifications,
                    null,
                    false,
                )
            },
            update = { view ->
                val notificationCenter =
                    view.findViewById<LinearLayout>(R.id.notificationCenter)
                val controller = notificationController()
                notificationCenter.removeAllViews()

                val inflater = LayoutInflater.from(notificationCenter.context)
                notifications
                    .filter { it["active"] == true }
                    .forEach { notification ->
                        val row = inflater.inflate(
                            R.layout.notification_entry,
                            notificationCenter,
                            false,
                        )
                        val notificationFrame =
                            row.findViewById<LinearLayout>(R.id.notificationFrame)
                        val newNotificationIcon =
                            row.findViewById<ImageView>(R.id.newNotificationIcon)
                        val notificationText =
                            row.findViewById<TextView>(R.id.notificationText)
                        val removeNotification =
                            row.findViewById<ImageView>(R.id.removeNotification)
                        val objectId = notification["objectId"]?.toString()
                        val request = notification["request"] as? Map<*, *>
                        val requestId = request?.get("objectId")?.toString()
                        val adopter = request?.get("userId") as? Map<*, *>
                        val decidedBy = request?.get("decidedBy") as? Map<*, *>
                        val animal = request?.get("animal") as? Map<*, *>

                        fun displayIdentity(user: Map<*, *>?): String {
                            val name = listOf(
                                user?.get("first")?.toString().orEmpty(),
                                user?.get("last")?.toString().orEmpty(),
                            ).filter { it.isNotBlank() }.joinToString(" ")
                            return name.ifBlank {
                                user?.get("name")?.toString().orEmpty().ifBlank {
                                    user?.get("email")?.toString().orEmpty()
                                }
                            }
                        }

                        newNotificationIcon.visibility =
                            if (notification["read"] == true) {
                                android.view.View.INVISIBLE
                            } else {
                                android.view.View.VISIBLE
                            }
                        val message = notification["message"]?.toString() ?: "Unknown"
                        val petName = animal?.get("name")?.toString().orEmpty()
                        val petSpecies = animal?.get("species")?.toString().orEmpty()
                        val petDetails = when {
                            petName.isNotBlank() && petSpecies.isNotBlank() ->
                                "Pet: $petName ($petSpecies)"
                            petName.isNotBlank() -> "Pet: $petName"
                            else -> null
                        }
                        val createdDate = when (val value = notification["created"]) {
                            is Date -> value
                            is Number -> Date(value.toLong())
                            else -> null
                        }
                        val timestamp = createdDate?.let {
                            "Received: ${DateFormat.getDateTimeInstance(
                                DateFormat.MEDIUM,
                                DateFormat.SHORT,
                            ).format(it)}"
                        }
                        val attribution = if (isAdmin) {
                            displayIdentity(adopter).takeIf { it.isNotBlank() }?.let { "Submitted by: $it" }
                        } else {
                            displayIdentity(decidedBy).takeIf { it.isNotBlank() }?.let { "Reviewed by: $it" }
                        }
                        notificationText.text = listOfNotNull(
                            message,
                            petDetails,
                            attribution,
                            timestamp,
                        ).joinToString("\n")

                        notificationFrame.setOnClickListener {
                            if (objectId != null && notification["read"] != true) {
                                controller.removeNotificationOrSetRead(
                                    objectId = objectId,
                                    changeReadValue = true,
                                    onSuccess = { refreshKey++ },
                                )
                            }
                            val destination = if (isAdmin) {
                                "manageRequests"
                            } else {
                                "adoptionApproval"
                            }
                            navController.navigate(
                                if (requestId.isNullOrBlank()) destination
                                else "$destination?requestId=$requestId"
                            ) {
                                launchSingleTop = true
                            }
                        }
                        removeNotification.setOnClickListener {
                            if (objectId != null) {
                                controller.removeNotificationOrSetRead(
                                    objectId = objectId,
                                    changeReadValue = false,
                                    onSuccess = { refreshKey++ },
                                )
                            }
                        }

                        notificationCenter.addView(row)
                    }
            },
        )
    }
}
