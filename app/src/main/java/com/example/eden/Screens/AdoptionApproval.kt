package com.example.eden.Screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eden.MainActivity
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import java.text.DateFormat
import java.util.Calendar

@Composable
fun AdoptionApproval(navController: NavController, focusedRequestId: String? = null) {
    val context = LocalContext.current
    val controller = remember { RequestWorkflowController() }
    var requests by remember { mutableStateOf<List<AdoptionRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var schedulingId by remember { mutableStateOf<String?>(null) }
    var fromMillis by remember { mutableStateOf<Long?>(null) }
    var toMillis by remember { mutableStateOf<Long?>(null) }
    var selectedSpecies by remember { mutableStateOf<String?>(null) }
    val filteredRequests = requests.filter {
        it.matchesFilters(fromMillis, toMillis, selectedSpecies)
    }
    val applicationPetIds = requests
        .filter { it.appointmentType.equals("ADOPTION", true) }
        .map { it.animalId }
        .filter { it.isNotBlank() }
        .toSet()

    fun loadRequests() {
        loading = true
        error = null
        controller.loadMyRequests(
            userId = MainActivity.loggedInUser?.objectId.orEmpty(),
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

    fun choosePickup(request: AdoptionRequest) {
        val selection = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                selection.set(year, month, day)
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        selection.set(Calendar.HOUR_OF_DAY, hour)
                        selection.set(Calendar.MINUTE, minute)
                        schedulingId = request.objectId
                        controller.schedulePickup(
                            requestId = request.objectId,
                            pickupTimestamp = selection.timeInMillis,
                            onSuccess = {
                                schedulingId = null
                                refreshKey++
                            },
                            onError = {
                                schedulingId = null
                                error = it
                            },
                        )
                    },
                    selection.get(Calendar.HOUR_OF_DAY),
                    selection.get(Calendar.MINUTE),
                    false,
                ).show()
            },
            selection.get(Calendar.YEAR),
            selection.get(Calendar.MONTH),
            selection.get(Calendar.DAY_OF_MONTH),
        ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
    }

    LaunchedEffect(refreshKey) { loadRequests() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController, "My Applications & Visits") },
        bottomBar = { BottomBar(navController) },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
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
                requests.isEmpty() -> Text("You have not submitted any applications or visit requests.")
                filteredRequests.isEmpty() -> Text("No requests match these filters.")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredRequests, key = { it.objectId }) { request ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(request.animalName, style = MaterialTheme.typography.titleMedium)
                                Text("Type: ${request.appointmentType}")
                                Text("Status: ${request.requestStatus}")
                                if (
                                    !request.requestStatus.equals("PENDING", true) &&
                                    (request.decidedByName.isNotBlank() || request.decidedByEmail.isNotBlank())
                                ) {
                                    val reviewer = request.decidedByName.ifBlank { request.decidedByEmail }
                                    Text("Reviewed by: $reviewer")
                                }
                                request.appointmentDate?.let {
                                    Text("Visit: ${DateFormat.getDateTimeInstance().format(it)}")
                                }
                                request.pickupDate?.let {
                                    Text("Pickup: ${DateFormat.getDateTimeInstance().format(it)}")
                                }
                                if (request.notes.isNotBlank()) Text("Notes: ${request.notes}")
                                if (
                                    request.appointmentType.equals("VISITATION", true) &&
                                    request.animalId.isNotBlank()
                                ) {
                                    Spacer(Modifier.height(12.dp))
                                    val alreadyApplied = request.animalId in applicationPetIds
                                    Button(
                                        enabled = !alreadyApplied,
                                        onClick = {
                                            MainActivity.chosenPetGlobal = hashMapOf(
                                                "objectId" to request.animalId,
                                                "name" to request.animalName,
                                                "status" to request.animalStatus.ifBlank { "Available" },
                                            )
                                            navController.navigate("adoptionForm")
                                        },
                                    ) {
                                        Text(
                                            if (alreadyApplied) "Application already submitted"
                                            else "Apply to adopt ${request.animalName}",
                                        )
                                    }
                                }
                                if (
                                    request.appointmentType.equals("ADOPTION", true) &&
                                    request.requestStatus.equals("ACCEPTED", true)
                                ) {
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        enabled = schedulingId == null,
                                        onClick = { choosePickup(request) },
                                    ) {
                                        Text(if (schedulingId == request.objectId) "Saving…" else "Schedule pickup")
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
