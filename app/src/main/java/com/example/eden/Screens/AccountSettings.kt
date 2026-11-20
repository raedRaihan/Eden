package com.example.eden.Screens

import com.example.eden.R
import androidx.compose.ui.res.stringResource
import android.util.Patterns
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.backendless.BackendlessUser
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.LogOutButton
import com.example.eden.uiElements.TopBar

@Composable
fun AccountSettings(
    navController: NavController,
    user: BackendlessUser?,
    onUserUpdated: (BackendlessUser) -> Unit
) {
    val controller = remember { AccountSettingsController() }
    val originalEmail = user?.getProperty("email")?.toString().orEmpty()
    var first by remember(user?.objectId) {
        mutableStateOf(user?.getProperty("first")?.toString().orEmpty())
    }
    var last by remember(user?.objectId) {
        mutableStateOf(user?.getProperty("last")?.toString().orEmpty())
    }
    var email by remember(user?.objectId) { mutableStateOf(originalEmail) }
    var profilePassword by remember { mutableStateOf("") }
    var profileLoading by remember { mutableStateOf(false) }
    var profileMessage by remember { mutableStateOf<String?>(null) }
    var profileError by remember { mutableStateOf<String?>(null) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordLoading by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isPasswordAccount =
        !user?.getProperty("accountType")?.toString().equals("OAUTH", ignoreCase = true)

    fun saveProfile() {
        profileError = profileValidationError(
            user != null, first, last,
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches(), profilePassword
        )
        if (profileError != null) return

        profileLoading = true
        profileMessage = null
        val emailChanged = !email.trim().equals(originalEmail, ignoreCase = true)
        controller.updateProfile(
            currentEmail = originalEmail,
            currentPassword = profilePassword,
            first = first,
            last = last,
            newEmail = email,
            onSuccess = { updatedUser ->
                profileLoading = false
                profilePassword = ""
                onUserUpdated(updatedUser)
                if (emailChanged) {
                    LogOutButton(navController)
                } else {
                    profileMessage = "Profile updated."
                }
            },
            onError = {
                profileLoading = false
                profileError = it
            }
        )
    }

    fun changePassword() {
        passwordError = passwordValidationError(
            user != null, isPasswordAccount, currentPassword, newPassword, confirmPassword
        )
        if (passwordError != null || user == null) return

        passwordLoading = true
        controller.updatePassword(
            email = originalEmail,
            currentPassword = currentPassword,
            newPassword = newPassword,
            onSuccess = { updatedUser ->
                passwordLoading = false
                onUserUpdated(updatedUser)
                LogOutButton(navController)
            },
            onError = {
                passwordLoading = false
                passwordError = it
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController, stringResource(R.string.screen_settings)) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Profile information", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = first,
                onValueChange = { first = it },
                label = { Text("First name") },
                enabled = !profileLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = last,
                onValueChange = { last = it },
                label = { Text("Last name") },
                enabled = !profileLoading,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email / username") },
                enabled = !profileLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = profilePassword,
                onValueChange = { profilePassword = it },
                label = { Text("Current password") },
                enabled = !profileLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            profileError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            profileMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Button(
                onClick = ::saveProfile,
                enabled = !profileLoading && !passwordLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (profileLoading) CircularProgressIndicator()
                else Text("Save Profile")
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text("Change password", style = MaterialTheme.typography.titleLarge)
            Text("You will be asked to log in again after changing your password.")

            if (!isPasswordAccount) {
                Text("This account uses a social login provider.")
            } else {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current password") },
                    enabled = !passwordLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New password") },
                    enabled = !passwordLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm new password") },
                    enabled = !passwordLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                passwordError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = ::changePassword,
                    enabled = !passwordLoading && !profileLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (passwordLoading) CircularProgressIndicator()
                    else Text("Change Password")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
