package com.example.eden.Screens

import android.os.Handler
import android.os.Looper
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

data class AdminUser(
    val objectId: String,
    val email: String,
    val first: String,
    val last: String,
    val userType: String,
    val userStatus: String
) {
    val displayName: String
        get() = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
}

class AdminUsersController {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun listUsers(
        onSuccess: (List<AdminUser>) -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.CustomService.invoke(
            "AdminService",
            "listUsers",
            emptyArray(),
            object : AsyncCallback<Array<BackendlessUser>> {
                override fun handleResponse(response: Array<BackendlessUser>) {
                    val users = response.mapNotNull { user ->
                        val objectId = user.objectId.orEmpty()
                        if (objectId.isBlank()) {
                            null
                        } else {
                            AdminUser(
                                objectId = objectId,
                                email = user.getProperty("email")?.toString().orEmpty(),
                                first = user.getProperty("first")?.toString().orEmpty(),
                                last = user.getProperty("last")?.toString().orEmpty(),
                                userType = user.getProperty("userType")?.toString().orEmpty(),
                                userStatus = user.getProperty("userStatus")?.toString().orEmpty()
                            )
                        }
                    }.sortedBy { it.email.lowercase() }

                    mainHandler.post { onSuccess(users) }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not load users.")
                    }
                }
            }
        )
    }

    fun setUserEnabled(
        userId: String,
        enabled: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.CustomService.invoke(
            "AdminService",
            "setUserEnabled",
            arrayOf<Any>(userId, enabled),
            object : AsyncCallback<Boolean> {
                override fun handleResponse(response: Boolean) {
                    mainHandler.post {
                        if (response) onSuccess()
                        else onError("Backendless did not update the user.")
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not update the user.")
                    }
                }
            }
        )
    }
}
