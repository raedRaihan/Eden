package com.example.eden.Screens

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import android.widget.Button
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

@Composable
fun AdoptionForm(navController: NavController, chosenPet: HashMap<*, *>)
{
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val newContext = ContextThemeWrapper(
                    context,
                    com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar
                )

                val view = LayoutInflater.from(newContext).inflate(R.layout.adoption_form, null, false)

                val title = view.findViewById<TextView>(R.id.afTemp)
                title.text = context.getString(
                    R.string.adoption_pet_title,
                    chosenPet["name"]?.toString() ?: context.getString(R.string.unknown_pet)
                )

                val backButton = view.findViewById<Button>(R.id.adoptionBackButton)

                backButton.setOnClickListener {
                    navController.popBackStack()
                }
                view
            }

        )

    }
}