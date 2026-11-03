package com.example.eden.Screens

import android.annotation.SuppressLint
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.MainActivity.Companion.queryResultsGlobal
import com.example.eden.R
import com.example.eden.appointments.MedicalRecordController
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import com.google.gson.JsonArray
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.get

@SuppressLint("MissingInflatedId")
@Composable
fun MedicalRecords(navController: NavController, chosenPet: HashMap<*, *>) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val newContext = ContextThemeWrapper(
                    context,
                    com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar
                )
                val view =
                    LayoutInflater.from(newContext).inflate(R.layout.medical_history, null, false)





                view
            } , update =  { view ->
                var doctorVisitsTable = view.findViewById<TableLayout>(R.id.doctorVisitsTable)

                var inflater = LayoutInflater.from(doctorVisitsTable.context)

                var MRC= MedicalRecordController()

                val animalId: String = chosenPet["objectId"] as String

                var lastUpdated=view.findViewById<TextView>(R.id.lastUpdated)
                var medications=view.findViewById<TextView>(R.id.medications)
                var vaccines=view.findViewById<TextView>(R.id.vaccines)
                var veterinarian=view.findViewById<TextView>(R.id.veterinarianName)
                var allergies = view.findViewById<TextView>(R.id.allergies)

                var goBackButton = view.findViewById<Button>(R.id.returnToPet)

                goBackButton.setOnClickListener {
                    navController.navigate("petProfile")
                }

                MRC.searchForRecords(animalId,
                    onSuccess= {    response ->

                        for(record in response)
                        {


                            if(record["visits"] != null)
                            {
                                val jsonValues = Gson().toJson(record["visits"])

                                val visits = Gson().fromJson(jsonValues, JsonArray::class.java)

                                for (visit in visits)
                                {
                                    val box = inflater.inflate(R.layout.doctor_visit, doctorVisitsTable, false)
                                    val tempVisit = visit.asJsonObject

                                    var dateText = tempVisit["date"]
                                    var treatmentNotesText = tempVisit["treatmentNotes"]
                                    var weightAndTempText = tempVisit["weightAndTemp"]

                                    box.findViewById<TextView>(R.id.doctorVisitDate).text = dateText?.asString ?: ""
                                    box.findViewById<TextView>(R.id.doctorVisitNotes).text = treatmentNotesText?.asString ?: ""
                                    box.findViewById<TextView>(R.id.doctorVisitWeight).text = weightAndTempText?.asString ?: ""

                                    doctorVisitsTable.addView(box)
                                }
                            }

                            var dateFormatter = SimpleDateFormat("M-d-yyyy", Locale.US)
                            lastUpdated.text = buildString {
                                append("Last Updated: ")
                                append(( (dateFormatter.format(record["dateFilledOut"])) ?: "None") as CharSequence?)
                            }

                            veterinarian.text = buildString {
                                append("Veterinarian: ")
                                append(( (record["veterinarian"].toString()) ?: "None") as CharSequence?)
                            }

                            medications.text = buildString {
                                append("Medications: ")
                                if(record["medications"] != null)
                                {
                                    append(( (record["medications"].toString()) ?: "None") as CharSequence?)
                                }
                                else
                                {
                                    append("None")
                                }
                            }

                            vaccines.text = buildString {
                                append("Vaccines: ")

                                if(record["vaccines"] != null)
                                {
                                    append(( (record["vaccines"].toString()) ?: "None") as CharSequence?)
                                }
                                else
                                {
                                    append("None")
                                }
                            }

                            allergies.text = buildString {
                                append("Allergies: ")
                                if(record["allergies"] != null)
                                {
                                    append(( (record["allergies"].toString()) ?: "None") as CharSequence?)
                                }
                                else
                                {
                                    append("None")
                                }

                            }






                        }
                    },
                    onError= {message -> Log.e("AppointmentController","Search faliure message: "+message)})




            }
        )

    }
}