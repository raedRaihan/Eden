package com.example.eden.Screens

import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.MainActivity.Companion.chosenPetGlobal
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import kotlin.collections.get

@SuppressLint("MissingInflatedId")
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

                val imageView = view.findViewById<ImageView>(R.id.petProfileImage)

                Glide.with(newContext).load(chosenPet["image"]).into(imageView)

                var petName=view.findViewById<TextView>(R.id.petProfileName)

                petName.text = buildString {
                    append("Pet Name: ")
                    append((chosenPet["name"] ?: "Unknown") as CharSequence?)
                }

                var petDescription=view.findViewById<TextView>(R.id.petProfileDescription)

                petDescription.text = buildString {
                    append("Bio: ")
                    append((chosenPet["description"] ?: "None") as CharSequence?)
                }

                var petSpecies=view.findViewById<TextView>(R.id.petProfileSpecies)

                petSpecies.text = buildString {
                    append("Species: ")
                    append((chosenPet["species"] ?: "Unknown") as CharSequence?)
                }

                var petBreed=view.findViewById<TextView>(R.id.petProfileBreed)

                petBreed.text = buildString {
                    append("Breed: ")
                    append((chosenPet["breed"] ?: "Unknown") as CharSequence?)
                }

                var petAge=view.findViewById<TextView>(R.id.petProfileAge)

                petAge.text = buildString {
                    append("Age: ")
                    append(( (chosenPet["ageMonths"].toString()) ?: "Unknown") as CharSequence?)
                    append(" months")
                }

                var petGender=view.findViewById<TextView>(R.id.petProfileGender)

                petGender.text = buildString {
                    append("Sex: ")
                    append((chosenPet["gender"] ?: "Unknown") as CharSequence?)
                }

                var petSize=view.findViewById<TextView>(R.id.petProfileSize)

                petSize.text = buildString {
                    append("Size: ")
                    append((chosenPet["size"] ?: "Unknown") as CharSequence?)
                }

                var petAvailability=view.findViewById<TextView>(R.id.petProfileAvailability)

                petAvailability.text = buildString {
                    append("Status: ")
                    append((chosenPet["status"] ?: "Unknown") as CharSequence?)
                }


                var adoptionButton=view.findViewById<Button>(R.id.adoptionButton)
                var visitButton=view.findViewById<Button>(R.id.visitButton)
                var recordsButton=view.findViewById<Button>(R.id.recordsButton)

                if(chosenPet["status"] != "Available")
                {
                    adoptionButton.isEnabled = false
                    visitButton.isEnabled = false
                    petAvailability.setTextColor(Color.parseColor("#8a0c03"))
                }

                adoptionButton.setOnClickListener {
                    navController.navigate("adoptionForm")
                }

                visitButton.setOnClickListener {
                    navController.navigate("visitationForm")
                }

                recordsButton.setOnClickListener {
                    navController.navigate("medicalRecords")
                }


                view
            }

        )

    }

}