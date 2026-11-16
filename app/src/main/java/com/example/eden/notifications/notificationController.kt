package com.example.eden.notifications

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder

class notificationController {
    fun searchForNotifications(email: String?, onSuccess: (List<Map<*, *>>) -> Unit, onError: (String) -> Unit
    ) {
        val searchQuery = DataQueryBuilder.create().setPageSize(100)



        searchQuery.setWhereClause("userEmail.email = '$email'")

        Backendless.Data.of("Notification").find(
            searchQuery,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {


                    if (response.isEmpty()) {
                        Log.d("notificationController", "No notifications found")
                    }
                    onSuccess(response)

                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e(
                        "notificationController",
                        "Could not find notifications: ${fault.message}"
                    )
                    onError(fault.message ?: "Could not find the notifications.")
                }
            }
        )
    }


    fun removeNotificationOrSetRead(objectId: String?, changeReadValue: Boolean, onSuccess: (Map<*, *>) -> Unit)
    {

        var searchQuery = hashMapOf<String, Any>(("objectId" to objectId) as Pair<String, Any>, "active" to false)
        if(changeReadValue)
        {
            searchQuery = hashMapOf<String, Any>(("objectId" to objectId) as Pair<String, Any>, "read" to true)
        }

        Backendless.Data.of("Notification").save(
            searchQuery,
            object : AsyncCallback<Map<*, *>> {
                override fun handleResponse(response: Map<*, *>) {

                    onSuccess(response)

                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e(
                        "notificationController",
                        "Could not find notifications: ${fault.message}"
                    )

                }

            }
        )
    }

    fun addNotification(email: String?, Newmessage: String?)
    {
        val newNotification = Notification(active = true, message = Newmessage, read = false)

        Backendless.Data.of(Notification::class.java).save(
            newNotification,
            object : AsyncCallback<Notification>{
                override fun handleResponse(response: Notification?){
                    Backendless.Data.of(Notification::class.java).setRelation(response,
                    "userEmail",
                        "email = '$email'",
                        object : AsyncCallback<Int> {

                            override fun handleResponse(response: Int?) {

                            }

                            override fun handleFault(fault: BackendlessFault?) {

                            }
                        })
                    Log.d("notificationController","Notification added!")


                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("notificationController","Error: "+ "${fault?.message}")
                }
            }
        )

    }



}
