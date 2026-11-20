package com.example.eden.Screens

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.backendless.property.ObjectProperty
import com.backendless.transaction.UnitOfWork
import com.backendless.transaction.UnitOfWorkResult
import java.util.Date

class AppointmentController {

    private fun notifyRequestSubmitted(requestId: String, onComplete: () -> Unit) {
        Backendless.CustomService.invoke(
            "AdminService",
            "notifyRequestSubmitted",
            arrayOf<Any>(requestId),
            object : AsyncCallback<Boolean> {
                override fun handleResponse(response: Boolean) {
                    if (!response) {
                        Log.w(
                            "AppointmentController",
                            "Request was saved, but Backendless did not confirm its notifications."
                        )
                    }
                    onComplete()
                }

                override fun handleFault(fault: BackendlessFault) {
                    // The Appointment transaction already succeeded. Do not tell the adopter
                    // that their request failed or encourage a duplicate submission.
                    Log.e(
                        "AppointmentController",
                        "Request was saved, but submission notifications failed: ${fault.message}"
                    )
                    onComplete()
                }
            }
        )
    }

    internal fun loadCalendarVisits(
        userId: String,
        isAdmin: Boolean = false,
        onSuccess: (List<CalendarVisit>) -> Unit,
        onError: () -> Unit
    ) {
        if (userId.isBlank()) {
            onError()
            return
        }

        val escapedUserId = userId.replace("'", "''")
        val now = System.currentTimeMillis()
        val whereClause = if (isAdmin) {
            "(appointmentType = 'VISITATION' AND requestStatus = 'CONFIRMED' " +
                "AND appointmentDate >= $now) OR " +
                "(appointmentType = 'ADOPTION' AND requestStatus = 'PICKUP_SCHEDULED' " +
                "AND pickupDate >= $now)"
        } else {
            "userId.objectId = '$escapedUserId' AND appointmentType = 'VISITATION'"
        }
        val visits = mutableListOf<CalendarVisit>()
        val pageSize = 100

        fun loadPage(offset: Int) {
            val query = DataQueryBuilder.create()
                .setWhereClause(whereClause)
                .setRelated("animal", "userId")
                .setPageSize(pageSize)
                .setOffset(offset)

            Backendless.Data.of("Appointment").find(
                query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        for (record in response) {
                            val appointmentType =
                                record["appointmentType"]?.toString().orEmpty()
                            val dateValue = if (appointmentType == "ADOPTION") {
                                record["pickupDate"]
                            } else {
                                record["appointmentDate"]
                            }
                            val timeMillis = when (dateValue) {
                                is Date -> dateValue.time
                                is Number -> dateValue.toLong()
                                else -> continue
                            }
                            val animal = record["animal"] as? Map<*, *>
                            val adopter = record["userId"] as? Map<*, *>
                            val adopterName = listOf(
                                adopter?.get("first")?.toString().orEmpty(),
                                adopter?.get("last")?.toString().orEmpty()
                            ).filter { it.isNotBlank() }.joinToString(" ").ifBlank {
                                adopter?.get("email")?.toString().orEmpty()
                            }
                            visits.add(
                                CalendarVisit(
                                    timeMillis = timeMillis,
                                    petId = animal?.get("objectId")?.toString().orEmpty(),
                                    petName = animal?.get("name") as? String,
                                    petStatus = animal?.get("status")?.toString().orEmpty(),
                                    status = record["requestStatus"]?.toString().orEmpty(),
                                    appointmentType = appointmentType,
                                    adopterName = adopterName
                                )
                            )
                        }

                        if (response.size == pageSize) {
                            loadPage(offset + response.size)
                        } else {
                            onSuccess(visits.sortedBy { it.timeMillis })
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.e("AppointmentController", "Could not load calendar appointments: " + fault.message)
                        onError()
                    }
                }
            )
        }

        loadPage(0)
    }

    fun findVisitAppointment(
        userId: String,
        petId: String,
        onSuccess: (Appointment?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (petId.isBlank() || userId.isBlank()) {
            onError("UserId not found!")
            return
        }


            val query = DataQueryBuilder.create()
                .setWhereClause("userId.objectId = '$userId' AND " +
                        "animal.objectId = '$petId' AND " +
                        "appointmentType = 'VISITATION' AND " +
                        "(requestStatus = 'PENDING' OR requestStatus = 'CONFIRMED')")
                .setSortBy("appointmentDate", "objectId")
                .setPageSize(1)

            Backendless.Data.of("Appointment").find(query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        val record = response.firstOrNull()

                        if (record == null) {
                            onSuccess(null)
                            return
                        }

                        val objectId = record["objectId"]?.toString()
                        if (objectId.isNullOrBlank()) {
                            onError("The appointment is missing its ID.")
                            return
                        }

                        val dateAndTime = when (val value = record["appointmentDate"]) {
                            is Date -> value
                            is Number -> Date(value.toLong())
                            else -> null
                        }

                        if (dateAndTime == null) {
                            onError("The appointment has an invalid date.")
                            return
                        }

                        val appointment = Appointment(
                            objectId = objectId,
                            dateAndTime = dateAndTime,
                            requestStatus = record["requestStatus"]?.toString().orEmpty(),
                            notes = record["notes"]?.toString().orEmpty()
                        )

                        onSuccess(appointment)

                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.e("AppointmentController", "Could not find appointment: ${fault.message}")
                        onError(fault.message ?: "Could not find the appointment.")
                    }
                }
            )

    }
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
                        if (response.isSuccess) {
                            notifyRequestSubmitted(requestId, onSuccess)
                        }
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

    fun submitAdoption(
        requestId: String,
        petId: String,
        userId: String,
        housingType: String,
        experienceLevel: String,
        hasOtherPets: Boolean,
        hoursAloneRange: String,
        adoptionReason: String,
        responsibilityAccepted: Boolean,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val applicationText = adoptionReason.trim()

        if (requestId.isBlank() || petId.isBlank() || userId.isBlank()) {
            onError("Sign in and select a pet before applying.")
            return
        }

        if (applicationText.isBlank() || applicationText.length > 250) {
            onError("Explain why you want to adopt this pet in 250 characters or fewer.")
            return
        }

        if (!responsibilityAccepted) {
            onError("Accept the adoption responsibility statement before submitting.")
            return
        }

        val transaction = UnitOfWork()

        transaction.upsert(
            "Appointment",
            mapOf(
                "objectId" to requestId,
                "appointmentType" to "ADOPTION",
                "requestStatus" to "PENDING",
                "housingType" to housingType,
                "experienceLevel" to experienceLevel,
                "hasOtherPets" to hasOtherPets,
                "hoursAloneRange" to hoursAloneRange,
                "adoptionReason" to applicationText,
                "responsibilityAccepted" to responsibilityAccepted,
                "notes" to notes.trim()
            )
        )

        transaction.setRelation(
            "Appointment", requestId, "animal", arrayOf(petId)
        )

        transaction.setRelation(
            "Appointment", requestId, "userId", arrayOf(userId)
        )

        transaction.execute(
            object : AsyncCallback<UnitOfWorkResult> {
                override fun handleResponse(response: UnitOfWorkResult) {
                    if (response.isSuccess) {
                        notifyRequestSubmitted(requestId, onSuccess)
                    } else {
                        onError("Could not save your application. Please try again.")
                    }
                }

                override fun handleFault(fault: BackendlessFault) {
                    onError(
                        fault.message ?: "Could not submit your application."
                    )
                }
            }
        )
    }

    fun findAdoptionApplication(
        userId: String,
        petId: String,
        onSuccess: (AdoptionApplication?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId.isBlank() || petId.isBlank()) {
            onError("Sign in and select a pet first.")
            return
        }

        val query = DataQueryBuilder.create()
            .setWhereClause(
                "userId.objectId = '$userId' AND " +
                        "animal.objectId = '$petId' AND " +
                        "appointmentType = 'ADOPTION'"
            )
            .setSortBy("created", "objectId")
            .setPageSize(1)

        Backendless.Data.of("Appointment").find(
            query,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    val record = response.firstOrNull()

                    if (record == null) {
                        onSuccess(null)
                        return
                    }

                    val objectId = record["objectId"]?.toString()

                    if (objectId.isNullOrBlank()) {
                        onError("The application is missing its ID.")
                        return
                    }

                    val application = AdoptionApplication(
                        objectId = objectId,
                        requestStatus = record["requestStatus"]
                            ?.toString().orEmpty(),
                        notes = record["notes"]?.toString().orEmpty()
                    )

                    onSuccess(application)
                }

                override fun handleFault(fault: BackendlessFault) {
                    onError(
                        fault.message ?: "Could not load your application."
                    )
                }
            }
        )
    }

    fun cancelVisit(
        appointmentId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (appointmentId.isBlank()) {
            onError("Appointment ID is missing.")
            return
        }

        val appointment = mapOf("objectId" to appointmentId)

        Backendless.Data.of("Appointment").remove(
            appointment,
            object : AsyncCallback<Long> {
                override fun handleResponse(response: Long) {
                    onSuccess()
                }

                override fun handleFault(fault: BackendlessFault) {
                    onError(fault.message ?: "Could not cancel the appointment.")
                }
            }
        )
    }
}
