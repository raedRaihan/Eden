package com.example.eden.Screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PreferenceQuizPrompt(
    onTakeQuiz: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Find better pet matches") },
        text = {
            Text(
                "You have not completed the pet filtering quiz. " +
                    "Would you like to choose your pet preferences now?"
            )
        },
        confirmButton = {
            TextButton(onClick = onTakeQuiz) {
                Text("Take quiz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}
