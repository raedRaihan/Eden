package com.example.eden.Screens

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import com.example.eden.MainActivity.Companion.queryResultsGlobal
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import com.google.android.material.textfield.TextInputLayout



@Composable
fun Search(navController: NavController)
{
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = {context ->
                val newContext = ContextThemeWrapper(context, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar)

                // replace this with context
                val view = LayoutInflater.from(newContext).inflate(R.layout.search, null, false)

                val nameEntry: EditText = view.findViewById<EditText>(R.id.SearchName)
                val minAgeEntry: EditText = view.findViewById<EditText>(R.id.minAge)
                val maxAgeEntry: EditText = view.findViewById<EditText>(R.id.maxAge)



                val species = context.resources.getStringArray(R.array.species)
                var breeds = context.resources.getStringArray(R.array.breedRabbit)
                var sizes = context.resources.getStringArray(R.array.size)
                var genders = context.resources.getStringArray(R.array.gender)

                val breedDropDown = view.findViewById<TextInputLayout>(R.id.breed)

                val speciesAdapter = ArrayAdapter<String>(newContext, R.layout.dropdown_item, species)
                var breedAdapter = ArrayAdapter<String>(newContext, R.layout.dropdown_item, breeds)
                var sizeAdapter = ArrayAdapter<String>(newContext, R.layout.dropdown_item, sizes)
                var genderAdapter = ArrayAdapter<String>(newContext, R.layout.dropdown_item, genders)


                val autocompleteTV = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                val autocompleteTV2 = view.findViewById<AutoCompleteTextView>(R.id.test)

                val autocompleteSize = view.findViewById<AutoCompleteTextView>(R.id.sizeAuto)
                val autocompleteGender = view.findViewById<AutoCompleteTextView>(R.id.genderAuto)

                autocompleteTV.setText(species[0],false)
                breedDropDown.isEnabled = false

                var chosenSpecies = "None"
                // this part runs when the user has selected a species
                autocompleteTV.onItemClickListener = AdapterView.OnItemClickListener{ parent, _, position, _  ->
                    chosenSpecies = parent.getItemAtPosition(position).toString()



                    if(chosenSpecies != "None")
                    {
                        breedDropDown.isEnabled = true

                        when (chosenSpecies) {
                            "Cat" -> {
                                breeds = context.resources.getStringArray(R.array.breedCat)
                            }
                            "Dog" -> {
                                breeds = context.resources.getStringArray(R.array.breedDog)
                            }
                            "Rabbit" -> {
                                breeds = context.resources.getStringArray(R.array.breedRabbit)
                            }
                        }
                        breedAdapter = ArrayAdapter<String>(newContext, R.layout.dropdown_item, breeds)
                        autocompleteTV2.setAdapter<ArrayAdapter<String>>(breedAdapter)

                    }
                    else
                    {
                        breedDropDown.isEnabled = false
                        autocompleteTV2.setText("", false)
                        /*autocompleteTV2.dismissDropDown()
                        autocompleteTV2.clearFocus()*/

                    }
                }

                var chosenBreed = "None"
                autocompleteTV2.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->

                    chosenBreed = parent.getItemAtPosition(position).toString()

                }

                var chosenSize = "None"
                autocompleteSize.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->

                    chosenSize = parent.getItemAtPosition(position).toString()

                }

                var chosenGender = "None"
                autocompleteGender.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->

                    chosenGender = parent.getItemAtPosition(position).toString()

                }


                autocompleteTV.setAdapter<ArrayAdapter<String>>(speciesAdapter)
                autocompleteTV2.setAdapter<ArrayAdapter<String>>(breedAdapter)
                autocompleteSize.setAdapter<ArrayAdapter<String>>(sizeAdapter)
                autocompleteGender.setAdapter<ArrayAdapter<String>>(genderAdapter)

                //entry fields
                var petName=view.findViewById<EditText>(R.id.SearchName)
                var minAge=view.findViewById<EditText>(R.id.minAge)
                var MaxAge=view.findViewById<EditText>(R.id.maxAge)

                var submitButton=view.findViewById<Button>(R.id.submitButton)

                var pc = PetController()


                submitButton.setOnClickListener{
                    Log.e(
                        "Test",
                        "Button Pressed"
                    )
                    pc.searchForPet(name = nameEntry.text.toString(), minAge = minAgeEntry.text.toString(), maxAge = maxAgeEntry.text.toString(),
                        species = chosenSpecies, breed = chosenBreed, size = chosenSize,gender = chosenGender,

                        onSuccess= {    response ->

                                        for(animal in response)
                                        {
                                            var msg = "Name: "+ animal["name"] +" Age in Months: "+animal["ageMonths"] +" Species: "+animal["species"] + " Breed: "+animal["breed"]+" Size: "+animal["size"]+" Gender: "+animal["gender"]
                                            Log.d("PetController", msg)

                                        }

                                        queryResultsGlobal = response

                                        navController.navigate("searchResults")
                                        {
                                            popUpTo(navController.graph.id) { inclusive = false }
                                            launchSingleTop = true
                                        }

                                   },
                        onError= {message -> Log.e("PetController","Search faliure message: "+message)}
                    )


                }

                view
            }
        )

    }



}