package com.example.eden.records

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import java.util.Date

class MedicalRecordController
{
    fun searchForRecords(
        animalId: String?,
        onSuccess: (List<Map<*, *>>) -> Unit,
        onError: (String) -> Unit,
    ) {
        val targetId = animalId?.trim().orEmpty()
        if (targetId.isBlank()) {
            onSuccess(emptyList())
            return
        }

        val matchingRecords = mutableListOf<Map<*, *>>()
        val pageSize = 100

        fun associatedAnimalIds(record: Map<*, *>): Set<String> {
            fun collectIds(value: Any?): Set<String> = when (value) {
                null -> emptySet()
                is Map<*, *> -> buildSet {
                    value["objectId"]?.toString()?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
                    value["animalId"]?.let { addAll(collectIds(it)) }
                    value["animal"]?.let { addAll(collectIds(it)) }
                }
                is Iterable<*> -> value.flatMapTo(mutableSetOf()) { collectIds(it) }
                is Array<*> -> value.flatMapTo(mutableSetOf()) { collectIds(it) }
                else -> setOf(value.toString().trim()).filterTo(mutableSetOf()) { it.isNotBlank() }
            }

            return buildSet {
                listOf("animalid", "animalId", "animal", "petId", "pet").forEach { property ->
                    addAll(collectIds(record[property]))
                }
            }
        }

        fun loadPage(offset: Int) {
            val query = DataQueryBuilder.create()
                .setRelated("animalid")
                .setSortBy("updated DESC", "created DESC")
                .setPageSize(pageSize)
                .setOffset(offset)

            Backendless.Data.of("MedicalRecord").find(
                query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        matchingRecords += response.filter { record ->
                            associatedAnimalIds(record).any { it.equals(targetId, ignoreCase = true) }
                        }
                        if (response.size == pageSize) {
                            loadPage(offset + response.size)
                        } else {
                            if (matchingRecords.isEmpty()) {
                                val associations = response.map { record ->
                                    val recordId = record["objectId"]?.toString().orEmpty()
                                    "$recordId=${associatedAnimalIds(record)}"
                                }
                                Log.d(
                                    "MedicalRecordController",
                                    "No medical record assigned to animal $targetId; " +
                                        "loaded ${response.size} rows with associations $associations",
                                )
                            }
                            onSuccess(matchingRecords.toList())
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.e(
                            "MedicalRecordController",
                            "Could not find medical records: " + fault.message,
                        )
                        onError(fault.message ?: "Could not find the medical records.")
                    }
                },
            )
        }

        loadPage(0)
    }

    fun saveRecord(
        animalId: String,
        existingObjectId: String?,
        medications: String,
        vaccines: String,
        veterinarian: String,
        allergies: String,
        visits: List<Map<String, String>>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val record = hashMapOf<Any?, Any?>(
            "dateFilledOut" to Date(),
            "medications" to medications.trim(),
            "vaccines" to vaccines.trim(),
            "veterinarian" to veterinarian.trim(),
            "allergies" to allergies.trim(),
            "visits" to visits,
        )
        existingObjectId?.takeIf { it.isNotBlank() }?.let {
            record["objectId"] = it
        }

        Backendless.Data.of("MedicalRecord").save(
            record,
            object : AsyncCallback<Map<*, *>> {
                override fun handleResponse(response: Map<*, *>) {
                    val escapedAnimalId = animalId.replace("'", "\\'")
                    Backendless.Data.of("MedicalRecord").setRelation(
                        response,
                        "animalid",
                        "objectId = '$escapedAnimalId'",
                        object : AsyncCallback<Int> {
                            override fun handleResponse(relatedCount: Int?) {
                                if ((relatedCount ?: 0) > 0) {
                                    Log.d(
                                        "MedicalRecordController",
                                        "Assigned medical record ${response["objectId"]} to animal $animalId",
                                    )
                                    onSuccess()
                                } else {
                                    onError("The medical record was saved, but the pet was not assigned to it.")
                                }
                            }

                            override fun handleFault(fault: BackendlessFault) {
                                Log.e(
                                    "MedicalRecordController",
                                    "Could not assign medical record to animal: ${fault.message}",
                                )
                                onError(
                                    fault.message
                                        ?: "The medical record was saved, but could not be assigned to the pet.",
                                )
                            }
                        },
                    )
                }

                override fun handleFault(fault: BackendlessFault) {
                    onError(fault.message ?: "Could not save the medical record.")
                }
            },
        )
    }
}