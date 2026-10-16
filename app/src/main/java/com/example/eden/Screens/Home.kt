package com.example.eden.Screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.res.ColorStateList
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.R

@Composable
fun Home(navController: NavController) {
    val colors = MaterialTheme.colorScheme

    AndroidView(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        factory = { context ->
            val root = LayoutInflater.from(context).inflate(R.layout.dashboard, null)
            root.setBackgroundColor(colors.background.toArgb())
            val heading = root.findViewById<TextView>(R.id.dashboardHeading)
            heading.setBackgroundColor(colors.primary.toArgb())
            heading.setTextColor(colors.onPrimary.toArgb())
            val list = root.findViewById<ListView>(R.id.petList)
            val filter = root.findViewById<Spinner>(R.id.speciesFilter)
            filter.backgroundTintList = ColorStateList.valueOf(colors.primary.toArgb())
            val pets = listOf(
                Pet("Maple", "Dog", "Golden Retriever · 2 years"),
                Pet("Oliver", "Cat", "Domestic Shorthair · 3 years"),
                Pet("Clover", "Rabbit", "Holland Lop · 18 months")
            )

            fun showPets(selectedSpecies: String) {
                val visiblePets = pets.filter { pet ->
                    selectedSpecies == "All pets" || pet.species == selectedSpecies
                }
                val rows = visiblePets.map { pet ->
                    "${pet.name}\n${pet.species} · ${pet.description}"
                }
                list.adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, rows) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val row = super.getView(position, convertView, parent) as TextView
                        row.setTextColor(colors.onBackground.toArgb())
                        return row
                    }
                }
            }

            filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    showPets(filter.selectedItem.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            showPets("All pets")
            root
        }
    )
}
