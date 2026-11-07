package com.example.eden.notifications

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder

class notificationController
{
    fun searchForNotifications(email: String?, onSuccess:(List<Map<*, *>>) -> Unit, onError: (String) -> Unit)
    {
        val searchQuery = DataQueryBuilder.create().setPageSize(100)

        //searchQuery.setWhereClause("userEmail = '$email'")
            //.setPageSize(1)

        Backendless.Data.of("Notifications").find(searchQuery,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    val record = response.firstOrNull()



                    if(response.isEmpty())
                    {
                        Log.d("notificationController", "No notifications found")
                    }
                    onSuccess(response)

                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("notificationController", "Could not find notifications: ${fault.message}")
                    onError(fault.message ?: "Could not find the notifications.")
                }
            }
        )
    }
}