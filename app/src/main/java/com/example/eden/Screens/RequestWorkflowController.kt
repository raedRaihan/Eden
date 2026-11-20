package com.example.eden.Screens

import android.os.Handler
import android.os.Looper
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import java.util.Date

data class AdoptionRequest(
    val objectId: String,
    val appointmentType: String,
    val requestStatus: String,
    val created: Date?,
    val notes: String,
    val housingType: String,
    val experienceLevel: String,
    val hasOtherPets: Boolean?,
    val hoursAloneRange: String,
    val adoptionReason: String,
    val responsibilityAccepted: Boolean,
    val appointmentDate: Date?,
    val pickupDate: Date?,
    val animalId: String,
    val animalName: String,
    val animalStatus: String,
    val animalSpecies: String,
    val adopterName: String,
    val adopterEmail: String,
    val decidedByName: String,
    val decidedByEmail: String,
)

class RequestWorkflowController {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadMyRequests(
        userId: String,
        onSuccess: (List<AdoptionRequest>) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (userId.isBlank()) {
            onError("Sign in to view your requests.")
            return
        }

        val escapedUserId = userId.replace("'", "''")
        val query = DataQueryBuilder.create()
            .setWhereClause(
                "userId.objectId = '$escapedUserId' AND " +
                    "(appointmentType = 'ADOPTION' OR appointmentType = 'VISITATION')"
            )
            .setRelated("animal", "userId", "decidedBy")
            .setSortBy("created DESC")
            .setPageSize(100)

        Backendless.Data.of("Appointment").find(
            query,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    mainHandler.post { onSuccess(response.mapNotNull(::toRequest)) }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not load your requests.")
                    }
                }
            },
        )
    }

    fun listAdminRequests(
        onSuccess: (List<AdoptionRequest>) -> Unit,
        onError: (String) -> Unit,
    ) {
        Backendless.CustomService.invoke(
            "AdminService",
            "listRequests",
            emptyArray(),
            object : AsyncCallback<Array<Any?>> {
                override fun handleResponse(response: Array<Any?>) {
                    val requests = response.mapNotNull { item ->
                        (item as? Map<*, *>)?.let(::toRequest)
                    }
                    mainHandler.post { onSuccess(requests) }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not load adoption and visitation requests.")
                    }
                }
            },
        )
    }

    fun decideRequest(
        requestId: String,
        accepted: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        Backendless.CustomService.invoke(
            "AdminService",
            "decideRequest",
            arrayOf<Any>(requestId, accepted),
            booleanCallback(onSuccess, onError, "The request was not updated."),
        )
    }

    fun schedulePickup(
        requestId: String,
        pickupTimestamp: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        Backendless.CustomService.invoke(
            "AdminService",
            "schedulePickup",
            arrayOf<Any>(requestId, pickupTimestamp),
            object : AsyncCallback<Boolean> {
                override fun handleResponse(response: Boolean?) {
                    when (response) {
                        true -> mainHandler.post(onSuccess)
                        false -> mainHandler.post {
                            onError("The pickup time was not saved.")
                        }
                        null -> verifyPickupScheduled(
                            requestId = requestId,
                            pickupTimestamp = pickupTimestamp,
                            onSuccess = onSuccess,
                            onError = onError,
                        )
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "The pickup time was not saved.")
                    }
                }
            },
        )
    }

    private fun verifyPickupScheduled(
        requestId: String,
        pickupTimestamp: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val escapedRequestId = requestId.replace("'", "''")
        val query = DataQueryBuilder.create()
            .setWhereClause("objectId = '$escapedRequestId'")
            .setPageSize(1)

        Backendless.Data.of("Appointment").find(
            query,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    val request = response.firstOrNull()
                    val savedPickup = request?.get("pickupDate").asDate()?.time
                    val savedStatus = request?.get("requestStatus")?.toString()
                    val timestampMatches = savedPickup != null &&
                        kotlin.math.abs(savedPickup - pickupTimestamp) < 60_000L
                    mainHandler.post {
                        if (savedStatus == "PICKUP_SCHEDULED" && timestampMatches) {
                            onSuccess()
                        } else {
                            onError(
                                "The pickup time was not saved. " +
                                    "The server returned an empty response and no matching update was found."
                            )
                        }
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(
                            fault.message
                                ?: "The server returned an empty response and the pickup could not be verified."
                        )
                    }
                }
            },
        )
    }

    private fun booleanCallback(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        falseMessage: String,
    ) = object : AsyncCallback<Boolean> {
        override fun handleResponse(response: Boolean?) {
            mainHandler.post {
                if (response == true) {
                    onSuccess()
                } else {
                    onError(
                        if (response == null) {
                            "$falseMessage The server returned an empty response."
                        } else {
                            falseMessage
                        }
                    )
                }
            }
        }

        override fun handleFault(fault: BackendlessFault) {
            mainHandler.post { onError(fault.message ?: falseMessage) }
        }
    }

    private fun toRequest(record: Map<*, *>): AdoptionRequest? {
        val objectId = record["objectId"]?.toString().orEmpty()
        if (objectId.isBlank()) return null

        val animal = record["animal"] as? Map<*, *>
        val adopter = record["userId"]
        val decidedBy = record["decidedBy"]
        val adopterEmail = adopter.userField("email")
        val adopterName = listOf(
            adopter.userField("first"),
            adopter.userField("last"),
        ).filter { it.isNotBlank() }.joinToString(" ")
        val decidedByName = listOf(
            decidedBy.userField("first"),
            decidedBy.userField("last"),
        ).filter { it.isNotBlank() }.joinToString(" ")

        return AdoptionRequest(
            objectId = objectId,
            appointmentType = record["appointmentType"]?.toString().orEmpty(),
            requestStatus = record["requestStatus"]?.toString().orEmpty(),
            created = record["created"].asDate(),
            notes = record["notes"]?.toString().orEmpty(),
            housingType = record["housingType"]?.toString().orEmpty(),
            experienceLevel = record["experienceLevel"]?.toString().orEmpty(),
            hasOtherPets = record["hasOtherPets"] as? Boolean,
            hoursAloneRange = record["hoursAloneRange"]?.toString().orEmpty(),
            adoptionReason = record["adoptionReason"]?.toString().orEmpty(),
            responsibilityAccepted = record["responsibilityAccepted"] == true,
            appointmentDate = record["appointmentDate"].asDate(),
            pickupDate = record["pickupDate"].asDate(),
            animalId = animal?.get("objectId")?.toString().orEmpty(),
            animalName = animal?.get("name")?.toString().orEmpty().ifBlank { "Unknown pet" },
            animalStatus = animal?.get("status")?.toString().orEmpty(),
            animalSpecies = animal?.get("species")?.toString().orEmpty(),
            adopterName = adopterName.ifBlank {
                adopter.userField("name").ifBlank {
                    adopterEmail.ifBlank { "Unknown adopter" }
                }
            },
            adopterEmail = adopterEmail,
            decidedByName = decidedByName.ifBlank {
                decidedBy.userField("name")
            },
            decidedByEmail = decidedBy.userField("email"),
        )
    }

    private fun Any?.userField(name: String): String = when (this) {
        is Map<*, *> -> get(name)?.toString().orEmpty()
        is BackendlessUser -> if (name == "objectId") {
            objectId.orEmpty()
        } else {
            getProperty(name)?.toString().orEmpty()
        }
        else -> ""
    }

    private fun Any?.asDate(): Date? = when (this) {
        is Date -> this
        is Number -> Date(toLong())
        else -> null
    }
}
