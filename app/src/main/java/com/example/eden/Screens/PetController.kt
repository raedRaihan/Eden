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
                                breed = breed,
                                gender = record["gender"]?.toString(),
                                image = record["image"]?.toString().orEmpty(),
                                status = record["status"]?.toString().orEmpty(),
                                ageMonths = (record["ageMonths"] as? Number)?.toDouble() ?: 0.0,
                                description = description,
                                objectId = record["objectId"]?.toString(),
                                record = record.toMap()
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
    fun searchForPet(name: String, minAge: String, maxAge: String, species: String, breed: String, size: String,gender: String,
    onSuccess:(List<Map<*, *>>) -> Unit, onError: (String) -> Unit)
    {
        val searchQuery = DataQueryBuilder.create().setPageSize(100)
        val whereClause = petSearchWhereClause(name, minAge, maxAge, species, breed, size, gender)
        if (whereClause.isNotBlank()) searchQuery.setWhereClause(whereClause)

        Backendless.Data.of("Animals").find(searchQuery,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {

                    if(response.isEmpty())
                    {
                        Log.d("PetController", "No pets found")
                    }
                    onSuccess(response)

                    for(animal in response)
                    {
                        //Log.d("PetController", "Name: "+ animal["name"] +" Age in Months: "+animal["ageMonths"]
                        //        +" Species: "+animal["species"] + " Breed: "+animal["breed"]+" Size: "+animal["size"]+" Gender: "+animal["gender"])
                    }



                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("PetController", "Could not load pets: ${fault.message}")
                    onError(fault.message ?: "Could not load pets.")
                }
            }
        )




    }

}
