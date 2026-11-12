package com.example.eden.records

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder

class MedicalRecordController
{
    fun searchForRecords(animalId: String?, onSuccess:(List<Map<*, *>>) -> Unit, onError: (String) -> Unit)
    {
        val searchQuery = DataQueryBuilder.create().setPageSize(100)

        searchQuery.setWhereClause("animalId = '$animalId'")
            .setPageSize(1)

        Backendless.Data.of("MedicalRecord").find(searchQuery,
            object : AsyncCallback<List<Map<*, *>>> {
                override fun handleResponse(response: List<Map<*, *>>) {
                    val record = response.firstOrNull()



                    if(response.isEmpty())
                    {
                        Log.d("MedicalRecordController", "No pets found")
                    }
                    onSuccess(response)

                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("MedicalRecordController", "Could not find medical records: ${fault.message}")
                    onError(fault.message ?: "Could not find the medical records.")
                }
            }
        )
    }
}