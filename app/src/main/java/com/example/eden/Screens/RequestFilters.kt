package com.example.eden.Screens

import android.app.DatePickerDialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Calendar

internal fun AdoptionRequest.matchesFilters(
    fromMillis: Long?,
    toMillis: Long?,
    species: String?,
): Boolean {
    val recordTime = created?.time ?: appointmentDate?.time
    return (species == null || animalSpecies.equals(species, true)) &&
        (fromMillis == null || recordTime == null || recordTime >= fromMillis) &&
        (toMillis == null || recordTime == null || recordTime <= toMillis)
}

@Composable
internal fun RequestFilters(
    speciesOptions: List<String>,
    selectedSpecies: String?,
    fromMillis: Long?,
    toMillis: Long?,
    onSpeciesChanged: (String?) -> Unit,
    onFromChanged: (Long?) -> Unit,
    onToChanged: (Long?) -> Unit,
) {
    val context = LocalContext.current
    val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM)

    fun showDatePicker(current: Long?, endOfDay: Boolean, onSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            if (current != null) timeInMillis = current
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                calendar.set(year, month, day)
                if (endOfDay) {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }
                onSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text("Filter by date submitted")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showDatePicker(fromMillis, false) { onFromChanged(it) } }) {
                Text(fromMillis?.let { "From ${formatter.format(it)}" } ?: "From date")
            }
            OutlinedButton(onClick = { showDatePicker(toMillis, true) { onToChanged(it) } }) {
                Text(toMillis?.let { "To ${formatter.format(it)}" } ?: "To date")
            }
        }
        Text("Pet type")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedSpecies == null,
                onClick = { onSpeciesChanged(null) },
                label = { Text("All") },
            )
            speciesOptions.forEach { species ->
                FilterChip(
                    selected = selectedSpecies == species,
                    onClick = { onSpeciesChanged(species) },
                    label = { Text(species) },
                )
            }
        }
        if (fromMillis != null || toMillis != null || selectedSpecies != null) {
            TextButton(onClick = {
                onFromChanged(null)
                onToChanged(null)
                onSpeciesChanged(null)
            }) { Text("Clear filters") }
        }
    }
}
