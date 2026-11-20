package com.example.eden.Screens

import android.os.Handler
import android.os.Looper
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

class AccountSettingsController {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun updateProfile(
        currentEmail: String,
        currentPassword: String,
        first: String,
        last: String,
        newEmail: String,
        phone: String,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        authenticate(
            email = currentEmail,
            password = currentPassword,
            onSuccess = { authenticatedUser ->
                authenticatedUser.setProperty("first", first.trim())
                authenticatedUser.setProperty("last", last.trim())
                authenticatedUser.setProperty("email", newEmail.trim())
                authenticatedUser.setProperty("phone", phone.trim().ifBlank { null })
                update(authenticatedUser, onSuccess, onError)
            },
            onError = onError
        )
    }

    fun updatePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        authenticate(
            email = email,
            password = currentPassword,
            onSuccess = { authenticatedUser ->
                authenticatedUser.setPassword(newPassword)
                update(authenticatedUser, onSuccess, onError)
            },
            onError = onError
        )
    }

    private fun authenticate(
        email: String,
        password: String,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.UserService.login(
            email,
            password,
            object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser) {
                    onSuccess(response)
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(
                            fault.message
                                ?: "Current password verification failed."
                        )
                    }
                }
            }
        )
    }

    private fun update(
        user: BackendlessUser,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.UserService.update(
            user,
            object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser) {
                    mainHandler.post { onSuccess(response) }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not update the account.")
                    }
                }
            }
        )
    }
}
