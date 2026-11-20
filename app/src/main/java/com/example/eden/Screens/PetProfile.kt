package com.example.eden.Screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.app.Dialog
import android.view.Window
import android.view.WindowManager
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

                Glide.with(view).load(chosenPet["image"]?.toString()?.trim())
                    .fallback(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imageView)

                imageView.setOnClickListener {
                    val dialog = Dialog(newContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                    val fullImage = ImageView(newContext).apply {
                        setBackgroundColor(Color.BLACK)
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        contentDescription = "Full-screen photo of ${chosenPet["name"] ?: "pet"}"
                        setOnClickListener { dialog.dismiss() }
                    }
                    dialog.setContentView(fullImage)
                    dialog.window?.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                    )
                    Glide.with(fullImage)
                        .load(chosenPet["image"]?.toString()?.trim())
                        .fitCenter()
                        .into(fullImage)
                    dialog.show()
                    dialog.window?.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                    )
                }

                var petName=view.findViewById<TextView>(R.id.petProfileName)

                petName.text = chosenPet["name"]?.toString().orEmpty().ifBlank { "Unknown pet" }

                var petDescription=view.findViewById<TextView>(R.id.petProfileDescription)

                petDescription.text = chosenPet["description"]?.toString().orEmpty()
                    .ifBlank { "This pet does not have a biography yet." }

                var petSpecies=view.findViewById<TextView>(R.id.petProfileSpecies)

                petSpecies.text = buildString {
                    append("SPECIES\n")
                    append((chosenPet["species"] ?: "Unknown") as CharSequence?)
                }

                var petBreed=view.findViewById<TextView>(R.id.petProfileBreed)

                petBreed.text = buildString {
                    append("BREED\n")
                    append((chosenPet["breed"] ?: "Unknown") as CharSequence?)
                }

                var petAge=view.findViewById<TextView>(R.id.petProfileAge)

                petAge.text = buildString {
                    append("AGE\n")
                    append(chosenPet["ageMonths"]?.toString() ?: "Unknown")
                    append(" months")
                }

                var petGender=view.findViewById<TextView>(R.id.petProfileGender)

                petGender.text = buildString {
                    append("GENDER\n")
                    append((chosenPet["gender"] ?: "Unknown") as CharSequence?)
                }

                var petSize=view.findViewById<TextView>(R.id.petProfileSize)

                petSize.text = buildString {
                    append("SIZE\n")
                    append((chosenPet["size"] ?: "Unknown") as CharSequence?)
                }

                var petAvailability=view.findViewById<TextView>(R.id.petProfileAvailability)

                petAvailability.text = buildString {
                    append((chosenPet["status"] ?: "Unknown") as CharSequence?)
                }


                var adoptionButton=view.findViewById<Button>(R.id.adoptionButton)
                var visitButton=view.findViewById<Button>(R.id.visitButton)
                var recordsButton=view.findViewById<Button>(R.id.recordsButton)

                if (com.example.eden.MainActivity.loggedInIsAdmin) {
                    adoptionButton.text = "Edit Pet Details"
                    visitButton.visibility = View.GONE
                    adoptionButton.setOnClickListener {
                        val petId = chosenPet["objectId"]?.toString().orEmpty()
                        navController.navigate(
                            if (petId.isBlank()) "manageAnimalProfiles"
                            else "manageAnimalProfiles?petId=$petId"
                        )
                    }
                } else {
                    adoptionButton.setOnClickListener {
                        navController.navigate("adoptionForm")
                    }

                    visitButton.setOnClickListener {
                        navController.navigate("visitationForm")
                    }
                }

                if(chosenPet["status"] != "Available")
                {
                    if (!com.example.eden.MainActivity.loggedInIsAdmin) {
                        adoptionButton.isEnabled = false
                    }
                    visitButton.isEnabled = false
                    petAvailability.setTextColor(Color.parseColor("#8a0c03"))
                } else {
                    petAvailability.setTextColor(Color.parseColor("#2E7D32"))
                }

                recordsButton.setOnClickListener {
                    navController.navigate("medicalRecords")
                }


                view
            }

        )

    }

}
