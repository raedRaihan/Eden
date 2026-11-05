package com.example.eden.Screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.ProgressBar
import android.content.res.ColorStateList
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.R
import com.example.eden.MainActivity.Companion.chosenPetGlobal
import com.example.eden.MainActivity.Companion.queryResultsGlobal
import com.example.eden.uiElements.TopBar
import com.example.eden.uiElements.BottomBar
import kotlin.collections.get

@Composable
fun Home(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val newContext = ContextThemeWrapper(context, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar)

                val root = LayoutInflater.from(newContext).inflate(R.layout.dashboard,null, false)


                root.setBackgroundColor(colors.background.toArgb())
                val heading = root.findViewById<TextView>(R.id.dashboardHeading)
                //heading.setBackgroundColor(colors.primary.toArgb())
                heading.setBackgroundColor(0xFF213d6c.toInt())
                heading.setTextColor(colors.onPrimary.toArgb())




                val filter = root.findViewById<Spinner>(R.id.speciesFilter)
               // filter.backgroundTintList = ColorStateList.valueOf(colors.primary.toArgb())
                filter.backgroundTintList = ColorStateList.valueOf(0xFF213d6c.toInt())
                val refresh = root.findViewById<Button>(R.id.refreshPets)
                val progress = root.findViewById<ProgressBar>(R.id.petsLoading)
                val message = root.findViewById<TextView>(R.id.petsMessage)
                //refresh.backgroundTintList = ColorStateList.valueOf(colors.primary.toArgb())
                refresh.backgroundTintList = ColorStateList.valueOf(0xFF213d6c.toInt())

                refresh.setTextColor(colors.onPrimary.toArgb())
                progress.indeterminateTintList = ColorStateList.valueOf(colors.primary.toArgb())
                message.setTextColor(colors.onBackground.toArgb())

                root.tag = true


                root


            },
            update =  { view ->

                var resultsQueryOut = view.findViewById<LinearLayout>(R.id.resultsQueryHome)

               // resultsQueryOut.removeAllViews()

                var inflater = LayoutInflater.from(resultsQueryOut.context)

                val refresh = view.findViewById<Button>(R.id.refreshPets)
                val spinner= view.findViewById<Spinner>(R.id.speciesFilter)

                var pc = PetController()

                fun loadPets(minAgeEntry: String, maxAgeEntry: String, chosenSpecies: String, chosenBreed: String, chosenSize: String,chosenGender: String)
                {


                    pc.searchForPet(name = "", minAge = minAgeEntry.toString(), maxAge = maxAgeEntry.toString(),
                        species = chosenSpecies, breed = chosenBreed, size = chosenSize,gender = chosenGender,

                        onSuccess= {    response ->

                            queryResultsGlobal = response
                            Log.d("queryResultsGlobal", queryResultsGlobal.toString())
                            for(pet in queryResultsGlobal)
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
                            refresh.isEnabled=true


                        },
                        onError= {message -> Log.e("PetController","Search faliure message: "+message)
                            refresh.isEnabled=true
                        }
                    )
                }



                var minAgeEntry = null
                var maxAgeEntry = null
                var chosenSpecies = "None"
                var chosenBreed = "None"
                var chosenSize = "None"
                var chosenGender = "None"

                loadPets(minAgeEntry = minAgeEntry.toString(), maxAgeEntry = maxAgeEntry.toString(),
                    chosenSpecies = chosenSpecies, chosenBreed = chosenBreed, chosenSize = chosenSize,chosenGender = chosenGender)



                refresh.setOnClickListener{


                    if(spinner.selectedItem.toString() == "All pets")
                    {
                        resultsQueryOut.removeAllViews()
                        chosenSpecies = "None"
                        loadPets(minAgeEntry = minAgeEntry.toString(), maxAgeEntry = maxAgeEntry.toString(),
                            chosenSpecies = chosenSpecies, chosenBreed = chosenBreed, chosenSize = chosenSize,chosenGender = chosenGender)
                        refresh.isEnabled=false
                    }
                    else if(spinner.selectedItem.toString() == "Dog")
                    {
                        resultsQueryOut.removeAllViews()
                        chosenSpecies = "Dog"
                        loadPets(minAgeEntry = minAgeEntry.toString(), maxAgeEntry = maxAgeEntry.toString(),
                            chosenSpecies = chosenSpecies, chosenBreed = chosenBreed, chosenSize = chosenSize,chosenGender = chosenGender)
                        refresh.isEnabled=false
                    }
                    else if(spinner.selectedItem.toString() == "Cat")
                    {
                        resultsQueryOut.removeAllViews()
                        chosenSpecies = "Cat"
                        loadPets(minAgeEntry = minAgeEntry.toString(), maxAgeEntry = maxAgeEntry.toString(),
                            chosenSpecies = chosenSpecies, chosenBreed = chosenBreed, chosenSize = chosenSize,chosenGender = chosenGender)
                        refresh.isEnabled=false
                    }
                    else if(spinner.selectedItem.toString() == "Rabbit")
                    {
                        resultsQueryOut.removeAllViews()
                        chosenSpecies = "Rabbit"
                        loadPets(minAgeEntry = minAgeEntry.toString(), maxAgeEntry = maxAgeEntry.toString(),
                            chosenSpecies = chosenSpecies, chosenBreed = chosenBreed, chosenSize = chosenSize,chosenGender = chosenGender)
                        refresh.isEnabled=false
                    }
                    else if(spinner.selectedItem.toString() == "User Preferences")
                    {

                    }
                }




            },
            onRelease = { it.tag = false }
        )
    }
}
