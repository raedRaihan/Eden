package com.example.eden.Screens

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.MainActivity.Companion.chosenPetGlobal
import com.example.eden.R
import com.example.eden.notifications.notificationController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import com.google.gson.Gson
import com.google.gson.JsonArray
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.get

@Composable
fun Notifications(navController: NavController, email: String)
{
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val newContext = ContextThemeWrapper(context, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar)

                val view = LayoutInflater.from(newContext).inflate(R.layout.notifications,null, false)

                Log.d("Notifications",("email name: "+email))
                var TestButton=view.findViewById<Button>(R.id.testNotification)

                TestButton.setOnClickListener {
                    val nC = notificationController()
                    nC.addNotification(email, "test notification")

                    navController.navigate("notifications")
                    val mBuilder = NotificationCompat.Builder(
                        newContext,
                        "Eden"

                    ).setSmallIcon(R.drawable.logo).setContentTitle("Eden").setContentText("test notification")

                }


                view

            },

            update =  { view ->

                var notificationCenter = view.findViewById<LinearLayout>(R.id.notificationCenter)

                //notificationCenter.removeAllViews()

                val nC = notificationController()
                //nC.addNotification(email, "test notification")

                var inflater = LayoutInflater.from(notificationCenter.context)

                nC.searchForNotifications(email,
                    onSuccess= {    response ->

                        for(notification in response) {
                            if (notification["active"] == true)
                            {
                                val box = inflater.inflate(R.layout.notification_entry, notificationCenter, false)
                                Log.d("Notifications",("Notification: "+notification))

                                val notificationFrame = box.findViewById<LinearLayout>(R.id.notificationFrame)
                                val newNotificationIcon=box.findViewById<ImageView>(R.id.newNotificationIcon)
                                val notificationText=box.findViewById<TextView>(R.id.notificationText)
                                val removeNotification=box.findViewById<ImageView>(R.id.removeNotification)

                                if(notification["read"] == true)
                                {
                                    newNotificationIcon.visibility= android.view.View.INVISIBLE
                                }


                                notificationFrame.setOnClickListener {
                                    Log.d("Notifications","Notification read")
                                    nC.removeNotificationOrSetRead(notification["objectId"].toString(), true,
                                        onSuccess= {   response ->  navController.navigate("notifications") }
                                        )

                                }

                                removeNotification.setOnClickListener {
                                    Log.d("Notifications","Notification removed")
                                    nC.removeNotificationOrSetRead(notification["objectId"].toString(), false,
                                        onSuccess= {    response ->  navController.navigate("notifications")}
                                        )


                                }

                                notificationText.text = buildString {
                                    append(notification["message"] ?: "Unknown")
                                }

                                notificationCenter.addView(box)
                            }

                        }
                    },
                    onError= {message -> Log.e("Notifications","Search faliure message: "+message)})

            }




        )

    }

}