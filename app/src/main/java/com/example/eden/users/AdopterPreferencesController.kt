package com.example.eden.users

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder

data class PetFilterOptions(
    val species: List<String>,
    val breeds: List<String>,
    val sizes: List<String>,
    val genders: List<String>
)

data class AdopterPreferences(
    val ageMinPreference: Int?,
    val ageMaxPreference: Int?,
    val breedPreference: String?,
    val genderPreference: String?,
    val speciesPreference: String?,
    val sizePreference: String?
)

class AdopterPreferencesController {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun loadOptions(
        onSuccess: (PetFilterOptions) -> Unit,
        onError: (String) -> Unit
    ) {
        val records = mutableListOf<Map<*, *>>()

        fun loadPage(offset: Int) {
            val query = DataQueryBuilder.create()
                .setWhereClause("status = 'Available'")
                .setPageSize(100)
                .setOffset(offset)

            Backendless.Data.of("Animals").find(
                query,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(response: List<Map<*, *>>) {
                        records.addAll(response)
                        if (response.size == 100) {
                            loadPage(offset + response.size)
                        } else {
                            val options = PetFilterOptions(
                                species = distinctValues(records, "species"),
                                breeds = distinctValues(records, "breed"),
                                sizes = distinctValues(records, "size"),
                                genders = distinctValues(records, "gender")
                            )
                            mainHandler.post { onSuccess(options) }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.e("AdopterPreferences", "Could not load filter options: ${fault.message}")
                        mainHandler.post {
                            onError(fault.message ?: "Could not load pet filter options.")
                        }
                    }
                }
            )
        }

        loadPage(0)
    }

    fun save(
        user: BackendlessUser,
        preferences: AdopterPreferences,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = user.getProperty("objectId")?.toString()
        if (userId.isNullOrBlank()) {
            onError("The logged-in user does not have an object ID.")
            return
        }

        val query = DataQueryBuilder.create()
            .setWhereClause("user.objectId = '${escapeForWhereClause(userId)}'")
            .setPageSize(1)

        Backendless.Data.of("Adopters").find(
            query,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    val adopter = HashMap<Any?, Any?>()
                    response.firstOrNull()?.get("objectId")?.let {
                        adopter["objectId"] = it
                    }
                    adopter["user"] = user
                    adopter["first"] = user.getProperty("first")
                    adopter["last"] = user.getProperty("last")
                    adopter["email"] = user.getProperty("email")
                    adopter["ageMinPreference"] = preferences.ageMinPreference
                    adopter["ageMaxPreference"] = preferences.ageMaxPreference
                    adopter["breedPreference"] = preferences.breedPreference
                    adopter["genderPreference"] = preferences.genderPreference
                    adopter["speciesPreference"] = preferences.speciesPreference
                    adopter["sizePreference"] = preferences.sizePreference

                    saveAdopter(adopter, onSuccess, onError)
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("AdopterPreferences", "Could not find adopter profile: ${fault.message}")
                    mainHandler.post {
                        onError(fault.message ?: "Could not find the adopter profile.")
                    }
                }
            }
        )
    }

    private fun saveAdopter(
        adopter: HashMap<Any?, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.Data.of("Adopters").save(
            adopter,
            object : AsyncCallback<Map<*, *>> {
                override fun handleResponse(response: Map<*, *>) {
                    mainHandler.post(onSuccess)
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("AdopterPreferences", "Could not save preferences: ${fault.message}")
                    mainHandler.post {
                        onError(fault.message ?: "Could not save pet preferences.")
                    }
                }
            }
        )
    }

    private fun distinctValues(records: List<Map<*, *>>, key: String): List<String> =
        records.mapNotNull { record ->
            record[key]?.toString()?.trim()?.takeIf(String::isNotEmpty)
        }.distinct().sorted()

    private fun escapeForWhereClause(value: String): String =
        value.replace("'", "''")
}
