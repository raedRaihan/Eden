package com.example.eden.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar

@Composable
fun ManageUsers(
    navController: NavController,
    currentUserId: String
) {
    val controller = remember { AdminUsersController() }
    var users by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var updatingUserId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingChange by remember { mutableStateOf<Pair<AdminUser, Boolean>?>(null) }

    fun loadUsers() {
        loading = true
        error = null
        controller.listUsers(
            onSuccess = {
                users = it
                loading = false
            },
            onError = {
                error = it
                loading = false
            }
        )
    }

    fun updateUser(user: AdminUser, enabled: Boolean) {
        updatingUserId = user.objectId
        error = null
        controller.setUserEnabled(
            userId = user.objectId,
            enabled = enabled,
            onSuccess = {
                updatingUserId = null
                loadUsers()
            },
            onError = {
                updatingUserId = null
                error = it
            }
        )
    }

    LaunchedEffect(Unit) { loadUsers() }

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
            Text("Manage Users", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = ::loadUsers,
                enabled = !loading && updatingUserId == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Users")
            }
            Spacer(Modifier.height(12.dp))

            if (loading) {
                CircularProgressIndicator()
            } else if (users.isEmpty()) {
                Text("No users were returned.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users, key = { it.objectId }) { user ->
                        val enabled = user.userStatus.equals("ENABLED", ignoreCase = true)
                        val isCurrentUser = user.objectId == currentUserId
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    user.displayName.ifBlank { "Unnamed user" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(user.email.ifBlank { "No email" })
                                Text(
                                    "Phone: ${user.phone.ifBlank { "Not provided" }}"
                                )
                                Text("Account type: ${user.userType.ifBlank { "Unknown" }}")
                                Text("Status: ${user.userStatus.ifBlank { "Unknown" }}")
                                if (isCurrentUser) {
                                    Text("Current administrator")
                                }
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = { pendingChange = user to !enabled },
                                    enabled = updatingUserId == null &&
                                        !(isCurrentUser && enabled),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (updatingUserId == user.objectId) {
                                        CircularProgressIndicator()
                                    } else {
                                        Text(if (enabled) "Disable User" else "Enable User")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingChange?.let { (user, enable) ->
        AlertDialog(
            onDismissRequest = { pendingChange = null },
            title = {
                Text(if (enable) "Enable user?" else "Disable user?")
            },
            text = {
                Text(
                    if (enable) {
                        "${user.email} will be able to log in again."
                    } else {
                        "${user.email} will no longer be able to log in."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingChange = null
                        updateUser(user, enable)
                    }
                ) {
                    Text(if (enable) "Enable" else "Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingChange = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
