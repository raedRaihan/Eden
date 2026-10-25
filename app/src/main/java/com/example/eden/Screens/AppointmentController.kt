package com.example.eden.Screens

import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.property.ObjectProperty
import com.backendless.transaction.UnitOfWork
import com.backendless.transaction.UnitOfWorkResult
import java.util.Date

class AppointmentController {
    fun submitVisit(
        requestId: String,
        petId: String,
        userId: String,
        visitTimeMillis: Long,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (petId.isBlank() || userId.isBlank() || visitTimeMillis <= System.currentTimeMillis()) {
            onError("Sign in, select a real pet, and choose a future visit time.")
            return
        }

        Backendless.Data.describe("Appointment", object : AsyncCallback<List<ObjectProperty>> {
            override fun handleResponse(response: List<ObjectProperty>) {
                val fields = response.associateBy { it.name }
                val stringsReady = listOf("appointmentType", "requestStatus", "notes").all {
                    fields[it]?.type?.toString() == "STRING"
                }
                val relationsReady = fields["animal"]?.relatedTable == "Animals" &&
                    fields["animal"]?.type?.toString() == "RELATION" &&
                    fields["userId"]?.relatedTable == "Users" &&
                    fields["userId"]?.type?.toString() == "RELATION"
                if (!stringsReady || !relationsReady || fields["appointmentDate"]?.type?.toString() != "DATETIME") {
                    onError("Appointment setup is incomplete. Check the required fields and relations in Backendless.")
                    return
                }
                if (visitTimeMillis <= System.currentTimeMillis()) {
                    onError("The selected visit time has passed. Choose a future time.")
                    return
                }

                val transaction = UnitOfWork()
                transaction.upsert("Appointment", mapOf(
                    "objectId" to requestId,
                    "appointmentDate" to Date(visitTimeMillis),
                    "appointmentType" to "VISITATION",
                    "requestStatus" to "PENDING",
                    "notes" to notes,
                ))
                transaction.setRelation("Appointment", requestId, "animal", arrayOf(petId))
                transaction.setRelation("Appointment", requestId, "userId", arrayOf(userId))
                transaction.execute(object : AsyncCallback<UnitOfWorkResult> {
                    override fun handleResponse(response: UnitOfWorkResult) {
                        if (response.isSuccess) onSuccess()
                        else onError("The visit request could not be saved. Please try again.")
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        onError(fault.message ?: "Could not submit the visit request.")
                    }
                })
            }

            override fun handleFault(fault: BackendlessFault) {
                onError(fault.message ?: "Could not check appointment setup.")
            }
        })
    }
}
