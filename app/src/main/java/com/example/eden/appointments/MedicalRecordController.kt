package com.example.eden.appointments

import android.util.Log
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.example.eden.Screens.Appointment
import java.util.Date

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
                        Log.d("PetController", "No pets found")
                    }
                    onSuccess(response)

                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("AppointmentController", "Could not find appointment: ${fault.message}")
                    onError(fault.message ?: "Could not find the appointment.")
                }
            }
        )
    }
}