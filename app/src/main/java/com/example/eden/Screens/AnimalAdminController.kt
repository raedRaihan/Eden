package com.example.eden.Screens

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors

data class AdminAnimal(
    val objectId: String? = null,
    val name: String,
    val ageMonths: Double,
    val species: String,
    val breed: String,
    val size: String,
    val gender: String,
    val description: String,
    val shelterName: String,
    val imageUrl: String,
    val status: String = "Available",
    val isDemo: Boolean = false
)

typealias NewAnimal = AdminAnimal

class AnimalAdminController {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val uploadExecutor = Executors.newSingleThreadExecutor()

    fun uploadImage(
        context: Context,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        uploadExecutor.execute {
            var temporaryFile: File? = null
            try {
                val mimeType = context.contentResolver.getType(uri).orEmpty()
                if (!mimeType.startsWith("image/")) {
                    throw IllegalArgumentException("Select a PNG or JPEG image.")
                }

                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/jpeg", "image/jpg" -> "jpg"
                    else -> "img"
                }
                temporaryFile = File.createTempFile(
                    "pet-${UUID.randomUUID()}-",
                    ".$extension",
                    context.cacheDir
                )
                context.contentResolver.openInputStream(uri)?.use { input ->
                    temporaryFile.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IllegalArgumentException("The selected image could not be opened.")

                if (temporaryFile.length() > 10L * 1024L * 1024L) {
                    throw IllegalArgumentException("The image must be 10 MB or smaller.")
                }

                val uploaded = Backendless.Files.upload(
                    temporaryFile,
                    "pet-images",
                    false
                )
                mainHandler.post { onSuccess(uploaded.fileURL) }
            } catch (error: Exception) {
                mainHandler.post {
                    onError(error.message ?: "Could not upload the pet image.")
                }
            } finally {
                temporaryFile?.delete()
            }
        }
    }

    fun loadAnimals(
        onSuccess: (List<AdminAnimal>) -> Unit,
        onError: (String) -> Unit
    ) {
        val animals = mutableListOf<AdminAnimal>()

        fun loadPage(offset: Int) {
            val query = DataQueryBuilder.create()
                .setSortBy("name", "objectId")
                .setPageSize(100)
                .setOffset(offset)

            Backendless.Data.of("Animals").find(
                query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        animals += response.mapNotNull(::toAdminAnimal)
                        if (response.size == 100) {
                            loadPage(offset + response.size)
                        } else {
                            mainHandler.post { onSuccess(animals.toList()) }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        mainHandler.post {
                            onError(fault.message ?: "Could not load animal profiles.")
                        }
                    }
                }
            )
        }

        loadPage(0)
    }

    fun createAnimal(
        animal: NewAnimal,
        onSuccess: (Map<*, *>) -> Unit,
        onError: (String) -> Unit
    ) = saveAnimal(animal, onSuccess, onError)

    fun saveAnimal(
        animal: AdminAnimal,
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
            "image" to animal.imageUrl.trim().takeIf { it.isNotEmpty() },
            "status" to animal.status.trim(),
            "isDemo" to animal.isDemo
        )
        animal.objectId?.takeIf { it.isNotBlank() }?.let {
            record["objectId"] = it
        }

        Backendless.Data.of("Animals").save(
            record,
            object : AsyncCallback<Map<*, *>> {
                override fun handleResponse(response: Map<*, *>) {
                    mainHandler.post { onSuccess(response) }
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not save the animal profile.")
                    }
                }
            }
        )
    }

    fun removeAnimal(
        objectId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.Data.of("Animals").remove(
            hashMapOf<Any?, Any?>("objectId" to objectId),
            object : AsyncCallback<Long> {
                override fun handleResponse(response: Long) {
                    mainHandler.post(onSuccess)
                }

                override fun handleFault(fault: BackendlessFault) {
                    mainHandler.post {
                        onError(fault.message ?: "Could not remove the animal profile.")
                    }
                }
            }
        )
    }

    private fun toAdminAnimal(record: Map<*, *>): AdminAnimal? {
        val objectId = record["objectId"]?.toString()?.takeIf { it.isNotBlank() }
            ?: return null
        return AdminAnimal(
            objectId = objectId,
            name = record["name"]?.toString().orEmpty(),
            ageMonths = (record["ageMonths"] as? Number)?.toDouble() ?: 0.0,
            species = record["species"]?.toString().orEmpty(),
            breed = record["breed"]?.toString().orEmpty(),
            size = record["size"]?.toString().orEmpty(),
            gender = record["gender"]?.toString().orEmpty(),
            description = record["description"]?.toString().orEmpty(),
            shelterName = record["shelterName"]?.toString().orEmpty(),
            imageUrl = record["image"]?.toString().orEmpty(),
            status = record["status"]?.toString().orEmpty().ifBlank { "Available" },
            isDemo = record["isDemo"] as? Boolean ?: false
        )
    }
}
