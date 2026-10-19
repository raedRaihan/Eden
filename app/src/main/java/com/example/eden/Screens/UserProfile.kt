package com.example.eden.Screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.backendless.BackendlessUser
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import com.example.eden.users.AdopterPreferences
import com.example.eden.users.AdopterPreferencesController
import com.example.eden.users.PetFilterOptions

@Composable
fun UserProfile(
    navController: NavController,
    first: String,
    last: String,
    email: String,
    accountType: String,
    backendlessUser: BackendlessUser?
) {
    var showQuiz by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf<PetFilterOptions?>(null) }
    var optionsError by remember { mutableStateOf<String?>(null) }
    var loadingOptions by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val preferencesController = remember { AdopterPreferencesController() }

    fun openQuiz() {
        showQuiz = true
        options = null
        optionsError = null
        loadingOptions = true
        preferencesController.loadOptions(
            onSuccess = {
                options = it
                loadingOptions = false
            },
            onError = {
                optionsError = it
                loadingOptions = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(24.dp))
            ProfileField("First name", first)
            ProfileField("Last name", last)
            ProfileField("Email", email)
            ProfileField("Account type", accountType)

            if (saveMessage != null) {
                Text(saveMessage!!, color = Color(0xFF2E7D32))
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = ::openQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pet filtering quiz")
            }
        }
    }

    if (showQuiz) {
        PetFilteringQuizDialog(
            options = options,
            loadingOptions = loadingOptions,
            optionsError = optionsError,
            backendlessUser = backendlessUser,
            controller = preferencesController,
            onSaved = {
                saveMessage = "Pet preferences saved."
                showQuiz = false
            },
            onDismiss = { showQuiz = false }
        )
    }
}

@Composable
private fun PetFilteringQuizDialog(
    options: PetFilterOptions?,
    loadingOptions: Boolean,
    optionsError: String?,
    backendlessUser: BackendlessUser?,
    controller: AdopterPreferencesController,
    onSaved: () -> Unit,
    onDismiss: () -> Unit
) {
    var minAge by remember { mutableStateOf("") }
    var maxAge by remember { mutableStateOf("") }
    var species by remember { mutableStateOf<String?>(null) }
    var breed by remember { mutableStateOf<String?>(null) }
    var size by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun save() {
        val minimum = minAge.takeIf { it.isNotBlank() }?.toIntOrNull()
        val maximum = maxAge.takeIf { it.isNotBlank() }?.toIntOrNull()
        validationError = when {
            minAge.isNotBlank() && minimum == null -> "Minimum age must be a whole number."
            maxAge.isNotBlank() && maximum == null -> "Maximum age must be a whole number."
            minimum != null && minimum < 0 -> "Minimum age cannot be negative."
            maximum != null && maximum < 0 -> "Maximum age cannot be negative."
            minimum != null && maximum != null && minimum > maximum ->
                "Minimum age cannot be greater than maximum age."
            backendlessUser == null -> "Log in before saving preferences."
            else -> null
        }

        val user = backendlessUser
        if (validationError != null || user == null) return

        saving = true
        controller.save(
            user = user,
            preferences = AdopterPreferences(
                ageMinPreference = minimum,
                ageMaxPreference = maximum,
                breedPreference = breed,
                genderPreference = gender,
                speciesPreference = species,
                sizePreference = size
            ),
            onSuccess = onSaved,
            onError = {
                saving = false
                validationError = it
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!saving) onDismiss()
        },
        title = { Text("Pet filtering quiz") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Every question is optional. Select Skip or leave an age blank.")

                AgeQuestion(
                    label = "Minimum age in months",
                    value = minAge,
                    enabled = !saving,
                    onValueChange = { minAge = it },
                    onSkip = { minAge = "" }
                )
                AgeQuestion(
                    label = "Maximum age in months",
                    value = maxAge,
                    enabled = !saving,
                    onValueChange = { maxAge = it },
                    onSkip = { maxAge = "" }
                )

                if (loadingOptions) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator()
                } else if (optionsError != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(optionsError, color = Color.Red)
                } else if (options != null) {
                    PreferenceQuestion("Species", options.species, species) { species = it }
                    PreferenceQuestion("Breed", options.breeds, breed) { breed = it }
                    PreferenceQuestion("Size", options.sizes, size) { size = it }
                    PreferenceQuestion("Gender", options.genders, gender) { gender = it }
                }

                if (validationError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(validationError!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = ::save,
                enabled = !saving && !loadingOptions && optionsError == null
            ) {
                Text(if (saving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AgeQuestion(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSkip: () -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(label, style = MaterialTheme.typography.titleSmall)
    Row {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.all(Char::isDigit)) onValueChange(newValue)
            },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        TextButton(onClick = onSkip, enabled = enabled) {
            Text("Skip")
        }
    }
}

@Composable
private fun PreferenceQuestion(
    label: String,
    choices: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    Spacer(Modifier.height(16.dp))
    Text(label, style = MaterialTheme.typography.titleSmall)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text("Skip") }
        )
        choices.forEach { choice ->
            FilterChip(
                selected = selected == choice,
                onClick = { onSelected(choice) },
                label = { Text(choice) }
            )
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Text(text = label, style = MaterialTheme.typography.labelLarge)
    Text(
        text = value.ifBlank { "Not provided" },
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(16.dp))
}
