package com.example.eden.Screens

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

private val breedsBySpecies = mapOf(
    "Cat" to listOf(
        "Domestic Shorthair", "Domestic Longhair", "Siamese", "Maine Coon",
        "Persian", "Ragdoll", "Tabby"
    ),
    "Dog" to listOf(
        "Beagle", "Labrador Retriever", "Golden Retriever", "German Shepherd",
        "Poodle", "Bulldog", "Mixed Breed"
    ),
    "Snake" to listOf(
        "Ball Python", "Corn Snake", "King Snake", "Milk Snake",
        "Garter Snake", "Boa Constrictor"
    ),
    "Rabbit" to listOf(
        "Holland Lop", "Mini Rex", "Netherland Dwarf", "Lionhead",
        "Dutch", "Flemish Giant"
    )
)
private val animalSizes = listOf("Small", "Medium", "Large")
private val animalGenders = listOf("Female", "Male", "Unknown")

@Composable
fun ManageAnimalProfiles(navController: NavController) {
    val controller = remember { AnimalAdminController() }
    var animals by remember { mutableStateOf<List<AdminAnimal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<AdminAnimal?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<AdminAnimal?>(null) }

    fun loadAnimals() {
        loading = true
        error = null
        controller.loadAnimals(
            onSuccess = {
                animals = it
                loading = false
            },
            onError = {
                error = it
                loading = false
            }
        )
    }

    LaunchedEffect(Unit) { loadAnimals() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Manage Pet Profiles", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { creating = true },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Pet Profile")
            }
            OutlinedButton(
                onClick = ::loadAnimals,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))

            when {
                loading -> CircularProgressIndicator()
                animals.isEmpty() -> Text("No animal profiles found.")
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(animals, key = { it.objectId.orEmpty() }) { animal ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (animal.imageUrl.isNotBlank()) {
                                    PetImage(
                                        imageUrl = animal.imageUrl,
                                        contentDescription = animal.name
                                    )
                                    Spacer(Modifier.height(10.dp))
                                }
                                Text(
                                    animal.name.ifBlank { "Unnamed pet" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    listOf(animal.species, animal.breed)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" · ")
                                        .ifBlank { "Species and breed not provided" }
                                )
                                Text("Status: ${animal.status}")
                                Text("Age: ${animal.ageMonths} months")
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { editing = animal },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Edit")
                                    }
                                    OutlinedButton(
                                        onClick = { deleting = animal },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        AnimalEditorDialog(
            initial = editing,
            controller = controller,
            onSaved = {
                creating = false
                editing = null
                loadAnimals()
            },
            onDismiss = {
                creating = false
                editing = null
            }
        )
    }

    deleting?.let { animal ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Remove pet profile?") },
            text = {
                Text(
                    "This permanently removes ${animal.name.ifBlank { "this animal" }} " +
                        "from Backendless."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val objectId = animal.objectId
                        deleting = null
                        if (objectId != null) {
                            loading = true
                            controller.removeAnimal(
                                objectId = objectId,
                                onSuccess = ::loadAnimals,
                                onError = {
                                    error = it
                                    loading = false
                                }
                            )
                        }
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AnimalEditorDialog(
    initial: AdminAnimal?,
    controller: AnimalAdminController,
    onSaved: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember(initial) { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var ageMonths by remember(initial) {
        mutableStateOf(initial?.ageMonths?.toString().orEmpty())
    }
    var species by remember(initial) { mutableStateOf(initial?.species.orEmpty()) }
    var breed by remember(initial) { mutableStateOf(initial?.breed.orEmpty()) }
    var size by remember(initial) { mutableStateOf(initial?.size.orEmpty()) }
    var gender by remember(initial) { mutableStateOf(initial?.gender.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var shelterName by remember(initial) {
        mutableStateOf(initial?.shelterName?.ifBlank { "Eden" } ?: "Eden")
    }
    var status by remember(initial) {
        mutableStateOf(initial?.status?.ifBlank { "Available" } ?: "Available")
    }
    var saving by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun save() {
        val age = ageMonths.toDoubleOrNull()
        validationError = when {
            name.isBlank() -> "Name is required."
            age == null || age < 0 -> "Age must be a non-negative number."
            species.isBlank() -> "Species is required."
            breed.isBlank() -> "Breed is required."
            size.isBlank() -> "Size is required."
            gender.isBlank() -> "Gender is required."
            description.isBlank() -> "Description is required."
            shelterName.isBlank() -> "Shelter name is required."
            status.isBlank() -> "Status is required."
            else -> null
        }
        if (validationError != null || age == null) return

        saving = true

        fun saveWithImage(imageUrl: String) {
            controller.saveAnimal(
                AdminAnimal(
                    objectId = initial?.objectId,
                    name = name,
                    ageMonths = age,
                    species = species,
                    breed = breed,
                    size = size,
                    gender = gender,
                    description = description,
                    shelterName = shelterName,
                    imageUrl = imageUrl,
                    status = status,
                    isDemo = initial?.isDemo ?: false
                ),
                onSuccess = { onSaved() },
                onError = {
                    saving = false
                    validationError = it
                }
            )
        }

        val imageUri = selectedImageUri
        if (imageUri == null) {
            saveWithImage(initial?.imageUrl.orEmpty())
        } else {
            controller.uploadImage(
                context = context,
                uri = imageUri,
                onSuccess = ::saveWithImage,
                onError = {
                    saving = false
                    validationError = it
                }
            )
        }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = {
            Text(if (initial == null) "Add Pet Profile" else "Edit Pet Profile")
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                EditorField("Name", name, !saving) { name = it }
                EditorField(
                    "Age in months",
                    ageMonths,
                    !saving,
                    KeyboardType.Decimal
                ) { ageMonths = it }
                SelectionField(
                    label = "Species",
                    value = species,
                    options = breedsBySpecies.keys.toList(),
                    enabled = !saving,
                    onSelected = {
                        if (species != it) breed = ""
                        species = it
                    }
                )
                SelectionField(
                    label = "Breed",
                    value = breed,
                    options = breedsBySpecies[species].orEmpty(),
                    enabled = !saving && breedsBySpecies.containsKey(species),
                    onSelected = { breed = it }
                )
                SelectionField(
                    label = "Size",
                    value = size,
                    options = animalSizes,
                    enabled = !saving,
                    onSelected = { size = it }
                )
                SelectionField(
                    label = "Gender",
                    value = gender,
                    options = animalGenders,
                    enabled = !saving,
                    onSelected = { gender = it }
                )
                EditorField("Description", description, !saving) { description = it }
                EditorField("Shelter name", shelterName, !saving) { shelterName = it }
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    enabled = !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        if (initial?.imageUrl.isNullOrBlank()) {
                            "Choose pet image"
                        } else {
                            "Replace pet image"
                        }
                    )
                }
                Text(
                    when {
                        selectedImageUri != null -> "New image selected"
                        !initial?.imageUrl.isNullOrBlank() -> "Current uploaded image will be kept"
                        else -> "No image selected"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                EditorField("Status", status, !saving) { status = it }
                validationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = ::save, enabled = !saving) {
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
private fun PetImage(
    imageUrl: String,
    contentDescription: String
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                this.contentDescription = contentDescription
            }
        },
        update = { imageView ->
            imageView.contentDescription = contentDescription
            Glide.with(imageView)
                .load(imageUrl)
                .centerCrop()
                .into(imageView)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled && options.isNotEmpty()) expanded = !expanded
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
