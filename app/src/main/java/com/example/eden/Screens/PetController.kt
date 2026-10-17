package com.example.eden.Screens

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder

class PetController {
    fun loadPets(onSuccess: (List<Pet>) -> Unit, onError: () -> Unit) {
        val pets = mutableListOf<Pet>()

        fun loadPage(offset: Int) {
            val query = DataQueryBuilder.create()
                .setWhereClause("status = 'Available'")
                .setSortBy("name", "objectId")
                .setPageSize(100)
                .setOffset(offset)

            Backendless.Data.of("Animals").find(query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        val page = response.map { record ->
                            val breed = record["breed"]?.toString().orEmpty()
                            val age = (record["ageMonths"] as? Number)?.toInt()
                            val details = record["description"]?.toString().orEmpty()
                            val description = listOfNotNull(
                                breed.takeIf { it.isNotBlank() },
                                age?.let { "$it months" },
                                details.takeIf { it.isNotBlank() }
                            ).joinToString(" · ")
                            Pet(
                                record["name"]?.toString() ?: "Unnamed pet",
                                record["species"]?.toString() ?: "Unknown species",
                                description
                            )
                        }
                        pets.addAll(page)
                        if (response.size == 100) {
                            loadPage(offset + response.size)
                        } else {
                            onSuccess(pets.toList())
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.e("PetController", "Could not load pets: ${fault.message}")
                        onError()
                    }
                }
            )
        }

        loadPage(0)
    }
}
