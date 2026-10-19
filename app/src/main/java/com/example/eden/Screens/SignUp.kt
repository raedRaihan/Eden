package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUp(
    loading: Boolean,
    backendError: String?,
    onSubmit: (first: String, last: String, email: String, password: String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        validationError = when {
            first.isBlank() || last.isBlank() || email.isBlank() ||
                password.isBlank() || confirmPassword.isBlank() ->
                "All fields are required."
            !email.contains("@") -> "Enter a valid email address."
            password != confirmPassword -> "Passwords do not match."
            else -> null
        }

        if (validationError == null) {
            onSubmit(
                first.trim(),
                last.trim(),
                email.trim(),
                password
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create an adopter account", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = first,
            onValueChange = { first = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("First name") },
            enabled = !loading,
            singleLine = true
        )
        OutlinedTextField(
            value = last,
            onValueChange = { last = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Last name") },
            enabled = !loading,
            singleLine = true
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            enabled = !loading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            enabled = !loading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            enabled = !loading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        val error = validationError ?: backendError
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = ::submit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text("Create account")
            }
        }
        OutlinedButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Back to login")
        }
    }
}
