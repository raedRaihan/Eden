package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CreateAnimalProfile(navController: NavController) {
    val controller = remember { AnimalAdminController() }
    var name by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var shelterName by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }

    fun submit() {
        val parsedAge = ageMonths.toDoubleOrNull()
        error = when {
            name.isBlank() -> "Name is required."
            parsedAge == null || parsedAge < 0 -> "Age must be a non-negative number of months."
            species.isBlank() -> "Species is required."
            breed.isBlank() -> "Breed is required."
            size.isBlank() -> "Size is required."
            gender.isBlank() -> "Gender is required."
            description.isBlank() -> "Description is required."
            shelterName.isBlank() -> "Shelter name is required."
            else -> null
        }
        if (error != null || parsedAge == null) return

        loading = true
        saved = false
        controller.createAnimal(
            NewAnimal(
                name = name,
                ageMonths = parsedAge,
                species = species,
                breed = breed,
                size = size,
                gender = gender,
                description = description,
                shelterName = shelterName,
                imageUrl = imageUrl
            ),
            onSuccess = {
                loading = false
                saved = true
                name = ""
                ageMonths = ""
                species = ""
                breed = ""
                size = ""
                gender = ""
                description = ""
                shelterName = ""
                imageUrl = ""
            },
            onError = {
                loading = false
                error = it
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create Animal Profile", style = MaterialTheme.typography.headlineMedium)
            Text("New profiles are created with Available status.")

            AnimalField("Name", name) { name = it }
            AnimalField(
                label = "Age in months",
                value = ageMonths,
                keyboardType = KeyboardType.Decimal
            ) { ageMonths = it }
            AnimalField("Species", species) { species = it }
            AnimalField("Breed", breed) { breed = it }
            AnimalField("Size", size) { size = it }
            AnimalField("Gender", gender) { gender = it }
            AnimalField("Description", description) { description = it }
            AnimalField("Shelter name", shelterName) { shelterName = it }
            AnimalField("Image URL (optional)", imageUrl) { imageUrl = it }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (saved) {
                Text("Animal profile created.", color = Color(0xFF2E7D32))
            }

            Button(
                onClick = ::submit,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator()
                else Text("Create Animal")
            }
            TextButton(
                onClick = { navController.popBackStack() },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Admin Dashboard")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnimalField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = label != "Description",
        modifier = Modifier.fillMaxWidth()
    )
}
