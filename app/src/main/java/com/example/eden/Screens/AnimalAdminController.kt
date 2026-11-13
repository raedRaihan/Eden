package com.example.eden.Screens

import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault

data class NewAnimal(
    val name: String,
    val ageMonths: Double,
    val species: String,
    val breed: String,
    val size: String,
    val gender: String,
    val description: String,
    val shelterName: String,
    val imageUrl: String
)

class AnimalAdminController {
    fun createAnimal(
        animal: NewAnimal,
        onSuccess: (Map<*, *>) -> Unit,
        onError: (String) -> Unit
    ) {
        val record = hashMapOf<Any?, Any?>(
            "name" to animal.name.trim(),
            "ageMonths" to animal.ageMonths,
            "species" to animal.species.trim(),
            "breed" to animal.breed.trim(),
            "size" to animal.size.trim(),
            "gender" to animal.gender.trim(),
            "description" to animal.description.trim(),
            "shelterName" to animal.shelterName.trim(),
            "status" to "Available",
            "isDemo" to false
        ).apply {
            if (animal.imageUrl.isNotBlank()) {
                put("image", animal.imageUrl.trim())
            }
        }

        Backendless.Data.of("Animals").save(
            record,
            object : AsyncCallback<Map<*, *>> {
                override fun handleResponse(response: Map<*, *>) = onSuccess(response)

                override fun handleFault(fault: BackendlessFault) {
                    onError(fault.message ?: "Could not create the animal profile.")
                }
            }
        )
    }
}
