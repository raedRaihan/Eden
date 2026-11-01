package com.example.eden.Screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.MainActivity.Companion.chosenPetGlobal
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

@SuppressLint("MissingInflatedId")
@Composable
fun SearchResults(navController: NavController, queryResults:List<Map<*, *>>)
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

                LayoutInflater.from(newContext).inflate(R.layout.search_results,null, false)

            },

            update =  { view ->

                var resultsQueryOut = view.findViewById<LinearLayout>(R.id.resultsQuery)

                resultsQueryOut.removeAllViews()

                var inflater = LayoutInflater.from(resultsQueryOut.context)
                Log.d("PetController","Query size "+queryResults.size.toString())

                for(pet in queryResults)
                {
                    Log.d("PetController","Type: "+pet::class.qualifiedName)
                    val box = inflater.inflate(R.layout.animal_entry, resultsQueryOut, false)

                    box.findViewById<TextView>(R.id.animalName).text = buildString {
                        append("Name: ")
                        append(pet["name"] ?: "Unknown")
                    }

                    val imageView=box.findViewById<ImageView>(R.id.animalPicture)

                    Glide.with(view).load(pet["image"]).into(imageView)

                    box.findViewById<TextView>(R.id.animalSpecies).text = buildString {
                        append("Species: ")
                        append(pet["species"] ?: "Unknown")
                    }

                    box.findViewById<TextView>(R.id.animalBreed).text = buildString {
                        append("Breed: ")
                        append(pet["breed"] ?: "Unknown")
                    }

                    box.findViewById<TextView>(R.id.animalAge).text = buildString {
                        append("Age: ")
                        append(pet["ageMonths"] ?: "Unknown")
                        append(" months")
                    }

                    box.findViewById<TextView>(R.id.animalSize).text = buildString {
                        append("Size: ")
                        append(pet["size"] ?: "Unknown")
                    }

                    box.findViewById<TextView>(R.id.animalGender).text = buildString {
                        append("Gender: ")
                        append(pet["gender"] ?: "Unknown")
                    }


                    box.setOnClickListener {
                        chosenPetGlobal = pet as HashMap<*, *>
                        navController.navigate("petProfile")
                    }

                    resultsQueryOut.addView(box)

                }

            }




        )

    }

}