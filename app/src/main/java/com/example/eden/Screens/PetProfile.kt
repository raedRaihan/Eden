package com.example.eden.Screens

import android.text.Editable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import kotlin.collections.get

@Composable
fun PetProfile(navController: NavController,  chosenPet: HashMap<*, *>)
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
                val view = LayoutInflater.from(newContext).inflate(R.layout.pet_profile, null, false)

                var adoptionButton=view.findViewById<Button>(R.id.adoptionButton)
                var visitButton=view.findViewById<Button>(R.id.visitButton)

                adoptionButton.setOnClickListener {
                    navController.navigate("adoptionForm")
                }

                visitButton.setOnClickListener {
                    navController.navigate("visitationForm")
                }

                val imageView = view.findViewById<ImageView>(R.id.myImageView)
                val imageUrl = "https://res.cloudinary.com/s4swftqd/image/upload/v1792979743/Beagle.webp"

                Glide.with(newContext).load(imageUrl).into(imageView)

                view
            }

        )

    }

}