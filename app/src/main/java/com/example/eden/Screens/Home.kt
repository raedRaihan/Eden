package com.example.eden.Screens

import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.MainActivity.Companion.chosenPetGlobal
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import com.example.eden.users.AdopterPreferencesController

@Composable
fun Home(navController: NavController, email: String) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val themedContext = ContextThemeWrapper(
                    context,
                    com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar
                )
                val inflater = LayoutInflater.from(themedContext)
                val root = inflater.inflate(R.layout.dashboard, null, false)
                val heading = root.findViewById<TextView>(R.id.dashboardHeading)
                val filter = root.findViewById<Spinner>(R.id.speciesFilter)
                val refresh = root.findViewById<Button>(R.id.refreshPets)
                val progress = root.findViewById<ProgressBar>(R.id.petsLoading)
                val message = root.findViewById<TextView>(R.id.petsMessage)
                val results = root.findViewById<LinearLayout>(R.id.resultsQueryHome)
                val controller = PetController()
                val accent = 0xFF213D6C.toInt()

                root.setBackgroundColor(colors.background.toArgb())
                heading.setBackgroundColor(accent)
                heading.setTextColor(colors.onPrimary.toArgb())
                filter.backgroundTintList = ColorStateList.valueOf(accent)
                refresh.backgroundTintList = ColorStateList.valueOf(accent)
                refresh.setTextColor(colors.onPrimary.toArgb())
                progress.indeterminateTintList = ColorStateList.valueOf(colors.primary.toArgb())
                message.setTextColor(colors.onBackground.toArgb())
                root.tag = true

                fun finishLoading() {
                    refresh.isEnabled = true
                    filter.isEnabled = true
                    progress.visibility = View.GONE
                }

                fun showPets(records: List<Map<*, *>>, emptyMessage: Int) {
                    results.removeAllViews()
                    message.setText(emptyMessage)
                    message.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE

                    for (pet in records) {
                        val card = inflater.inflate(R.layout.animal_entry, results, false)
                        card.findViewById<TextView>(R.id.animalName).text = "Name: ${pet["name"] ?: "Unknown"}"
                        card.findViewById<TextView>(R.id.animalSpecies).text = "Species: ${pet["species"] ?: "Unknown"}"
                        card.findViewById<TextView>(R.id.animalBreed).text = "Breed: ${pet["breed"] ?: "Unknown"}"
                        card.findViewById<TextView>(R.id.animalAge).text = "Age: ${pet["ageMonths"] ?: "Unknown"} months"
                        card.findViewById<TextView>(R.id.animalSize).text = "Size: ${pet["size"] ?: "Unknown"}"
                        card.findViewById<TextView>(R.id.animalGender).text = "Gender: ${pet["gender"] ?: "Unknown"}"

                        Glide.with(root).load(pet["image"]?.toString()?.trim())
                            .fallback(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .into(card.findViewById<ImageView>(R.id.animalPicture))

                        card.setOnClickListener {
                            chosenPetGlobal = HashMap(pet)
                            navController.navigate("petProfile")
                        }
                        results.addView(card)
                    }
                }

                fun loadPets() {
                    if (!refresh.isEnabled) return
                    val selection = filter.selectedItem.toString()
                    refresh.isEnabled = false
                    filter.isEnabled = false
                    progress.visibility = View.VISIBLE
                    message.visibility = View.VISIBLE
                    message.setText(R.string.pets_loading)
                    results.removeAllViews()

                    fun failed() {
                        root.post {
                            if (root.tag == true) {
                                finishLoading()
                                message.setText(R.string.pets_load_error)
                            }
                        }
                    }

                    fun loaded(records: List<Map<*, *>>, emptyMessage: Int) {
                        root.post {
                            if (root.tag == true) {
                                finishLoading()
                                showPets(records, emptyMessage)
                            }
                        }
                    }

                    fun loadAvailablePets(species: String = "All pets") {
                        controller.loadPets(
                            onSuccess = { pets ->
                                val matching = pets.filter { species == "All pets" || it.species == species }
                                loaded(
                                    matching.map { it.record },
                                    if (pets.isEmpty()) R.string.pets_empty else R.string.pets_no_matches
                                )
                            },
                            onError = ::failed
                        )
                    }

                    if (selection == "User Preferences" && email.isNotBlank()) {
                        AdopterPreferencesController().findPreferances(
                            email,
                            onSuccess = { preferences ->
                                val preference = preferences.firstOrNull()
                                if (preference == null) {
                                    loadAvailablePets()
                                } else {
                                    fun value(key: String, fallback: String = "None") =
                                        preference[key]?.toString()?.takeIf { it.isNotBlank() } ?: fallback

                                    controller.searchForPet(
                                        name = "",
                                        minAge = value("ageMinPreference", ""),
                                        maxAge = value("ageMaxPreference", ""),
                                        species = value("speciesPreference"),
                                        breed = value("breedPreference"),
                                        size = value("sizePreference"),
                                        gender = value("genderPreference"),
                                        onSuccess = { records ->
                                            loaded(records.filter { it["status"] == "Available" }, R.string.pets_no_preferences_matches)
                                        },
                                        onError = { failed() }
                                    )
                                }
                            },
                            onError = { failed() }
                        )
                    } else {
                        loadAvailablePets(if (selection == "User Preferences") "All pets" else selection)
                    }
                }

                filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        loadPets()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
                refresh.setOnClickListener { loadPets() }
                loadPets()
                root
            },
            onRelease = { it.tag = false }
        )
    }
}
