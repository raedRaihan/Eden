package com.example.eden.Screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import java.text.DateFormat

@Composable
fun ManageRequests(navController: NavController, focusedRequestId: String? = null) {
    val controller = remember { RequestWorkflowController() }
    var requests by remember { mutableStateOf<List<AdoptionRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var updatingId by remember { mutableStateOf<String?>(null) }
    var fromMillis by remember { mutableStateOf<Long?>(null) }
    var toMillis by remember { mutableStateOf<Long?>(null) }
    var selectedSpecies by remember { mutableStateOf<String?>(null) }
    val filteredRequests = requests.filter {
        it.matchesFilters(fromMillis, toMillis, selectedSpecies)
    }

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        controller.listAdminRequests(
            onSuccess = {
                requests = it.sortedByDescending { request -> request.objectId == focusedRequestId }
                loading = false
            },
            onError = {
                error = it
                loading = false
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController, "Manage Requests") },
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
        ) {
            if (!loading && error == null && requests.isNotEmpty()) {
                RequestFilters(
                    speciesOptions = requests.map { it.animalSpecies }
                        .filter { it.isNotBlank() }.distinct().sorted(),
                    selectedSpecies = selectedSpecies,
                    fromMillis = fromMillis,
                    toMillis = toMillis,
                    onSpeciesChanged = { selectedSpecies = it },
                    onFromChanged = { fromMillis = it },
                    onToChanged = { toMillis = it },
                )
            }
            when {
                loading -> CircularProgressIndicator()
                error != null -> {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { refreshKey++ }) { Text("Retry") }
                }
                requests.isEmpty() -> Text("No adoption applications or visit requests.")
                filteredRequests.isEmpty() -> Text("No requests match these filters.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredRequests, key = { it.objectId }) { request ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${request.appointmentType.lowercase().replaceFirstChar { it.uppercase() }} request",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text("Pet: ${request.animalName}")
                                Text("Adopter: ${request.adopterName}")
                                if (request.adopterEmail.isNotBlank()) Text(request.adopterEmail)
                                Text("Status: ${request.requestStatus}")
                                if (request.decidedByName.isNotBlank() || request.decidedByEmail.isNotBlank()) {
                                    val reviewer = request.decidedByName.ifBlank { request.decidedByEmail }
                                    Text("Decision made by: $reviewer")
                                }
                                request.appointmentDate?.let {
                                    Text("Visit: ${DateFormat.getDateTimeInstance().format(it)}")
                                }
                                if (request.notes.isNotBlank()) Text("Notes: ${request.notes}")
                                if (request.appointmentType.equals("ADOPTION", true)) {
                                    if (request.housingType.isNotBlank()) Text("Housing: ${request.housingType}")
                                    if (request.experienceLevel.isNotBlank()) Text("Experience: ${request.experienceLevel}")
                                    request.hasOtherPets?.let { Text("Other pets: ${if (it) "Yes" else "No"}") }
                                    if (request.hoursAloneRange.isNotBlank()) Text("Time alone: ${request.hoursAloneRange}")
                                    if (request.adoptionReason.isNotBlank()) Text("Why this pet: ${request.adoptionReason}")
                                    Text(
                                        "Responsibility agreement: ${if (request.responsibilityAccepted) "Accepted" else "Not accepted"}"
                                    )
                                }
                                request.pickupDate?.let {
                                    Text("Pickup: ${DateFormat.getDateTimeInstance().format(it)}")
                                }

                                if (request.requestStatus.equals("PENDING", true)) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            enabled = updatingId == null,
                                            onClick = {
                                                updatingId = request.objectId
                                                controller.decideRequest(
                                                    request.objectId,
                                                    true,
                                                    onSuccess = {
                                                        updatingId = null
                                                        refreshKey++
                                                    },
                                                    onError = {
                                                        updatingId = null
                                                        error = it
                                                    },
                                                )
                                            },
                                        ) { Text("Accept") }
                                        OutlinedButton(
                                            enabled = updatingId == null,
                                            onClick = {
                                                updatingId = request.objectId
                                                controller.decideRequest(
                                                    request.objectId,
                                                    false,
                                                    onSuccess = {
                                                        updatingId = null
                                                        refreshKey++
                                                    },
                                                    onError = {
                                                        updatingId = null
                                                        error = it
                                                    },
                                                )
                                            },
                                        ) { Text("Deny") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
