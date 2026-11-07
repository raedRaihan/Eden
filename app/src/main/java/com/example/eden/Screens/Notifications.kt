package com.example.eden.Screens

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
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

                val root = LayoutInflater.from(newContext).inflate(R.layout.notifications,null, false)
                val heading = root.findViewById<TextView>(R.id.notificationsTest)

                Log.d("Notifications",("email name: "+email))
                heading.text = "email name: "+email
                val nC = notificationController()

                nC.searchForNotifications(email,
                    onSuccess= {    response ->

                        for(notification in response)
                        {

                            Log.d("Notifications",("Notification: "+notification))





                        }
                    },
                    onError= {message -> Log.e("Notifications","Search faliure message: "+message)})



                root

            },

            update =  { view ->

                view

            }




        )

    }

}