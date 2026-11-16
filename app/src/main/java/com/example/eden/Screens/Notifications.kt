package com.example.eden.Screens

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
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

@Composable
fun Notifications(navController: NavController, email: String) {
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
                val testButton = view.findViewById<Button>(R.id.testNotification)
                val controller = notificationController()

                testButton.setOnClickListener {
                    controller.addNotification(
                        email = email,
                        message = "test notification",
                        onSuccess = { refreshKey++ },
                        onError = { message ->
                            Log.e("Notifications", "Create failure: $message")
                        },
                    )
                }

                // Keep the test button, but rebuild the data-dependent rows.
                while (notificationCenter.childCount > 1) {
                    notificationCenter.removeViewAt(1)
                }

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

                        newNotificationIcon.visibility =
                            if (notification["read"] == true) {
                                android.view.View.INVISIBLE
                            } else {
                                android.view.View.VISIBLE
                            }
                        notificationText.text =
                            notification["message"]?.toString() ?: "Unknown"

                        notificationFrame.setOnClickListener {
                            if (objectId != null && notification["read"] != true) {
                                controller.removeNotificationOrSetRead(
                                    objectId = objectId,
                                    changeReadValue = true,
                                    onSuccess = { refreshKey++ },
                                )
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
