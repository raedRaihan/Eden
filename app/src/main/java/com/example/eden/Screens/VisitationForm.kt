package com.example.eden.Screens

import android.content.res.ColorStateList
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.MainActivity
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import java.util.Calendar
import java.util.UUID

@Composable
fun VisitationForm(navController: NavController, chosenPet: HashMap<*, *>) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            factory = { context ->
                val themedContext = ContextThemeWrapper(
                    context,
                    com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar
                )
                val view = LayoutInflater.from(themedContext).inflate(R.layout.visitation_form, null, false)
                val title = view.findViewById<TextView>(R.id.vfTemp)
                val dateButton = view.findViewById<Button>(R.id.visitDateButton)
                val timeButton = view.findViewById<Button>(R.id.visitTimeButton)
                val backButton = view.findViewById<Button>(R.id.visitBackButton)
                val reviewButton = view.findViewById<Button>(R.id.visitReviewButton)
                val notesEditText = view.findViewById<EditText>(R.id.visitNotesInput)
                val errorMessage = view.findViewById<TextView>(R.id.visitErrorMessage)
                val visitDateTime = Calendar.getInstance()
                var dateSelected = false
                var timeSelected = false
                var submitting = false
                var submitted = false
                val requestId = UUID.randomUUID().toString()
                val bookingContainer = view.findViewById<LinearLayout>(R.id.bookingFormContainer)
                val existingContainer = view.findViewById<LinearLayout>(R.id.existingAppointmentContainer)
                val appointmentSummary = view.findViewById<TextView>(R.id.appointmentSummary)
                val lookupMessage = view.findViewById<TextView>(R.id.errorOrLoadText)
                val backButton2 = view.findViewById<Button>(R.id.visitBackButton2)
                val retryButton = view.findViewById<Button>(R.id.visitRetryButton)
                val loadingSpinner = view.findViewById<ProgressBar>(R.id.visitLoadingSpinner)
                loadingSpinner.indeterminateTintList = ColorStateList.valueOf(colors.primary.toArgb())

                val cancelButton = view.findViewById<Button>(R.id.cancelButton)

                var existingAppointment: Appointment? = null
                view.tag = true

                fun checkAppointment() {
                    bookingContainer.visibility = View.GONE
                    existingContainer.visibility = View.GONE
                    lookupMessage.visibility = View.GONE
                    loadingSpinner.visibility = View.VISIBLE
                    retryButton.visibility = View.GONE

                    val userId = MainActivity.loggedInUser?.objectId
                    val petId = chosenPet["objectId"]?.toString()

                    if (userId.isNullOrBlank() || petId.isNullOrBlank()) {
                        loadingSpinner.visibility = View.GONE
                        lookupMessage.setText(R.string.visit_lookup_identity_required)
                        lookupMessage.visibility = View.VISIBLE
                        return
                    }

                    AppointmentController().findVisitAppointment(
                        userId = userId,
                        petId = petId,
                        onSuccess = { appointment ->
                            view.post {
                                if (view.tag == true) {
                                    existingAppointment = appointment
                                    loadingSpinner.visibility = View.GONE
                                    lookupMessage.visibility = View.GONE

                                    if (appointment == null) {
                                        if (submitted) {
                                            lookupMessage.setText(R.string.visit_saved_reload)
                                            lookupMessage.visibility = View.VISIBLE
                                            retryButton.visibility = View.VISIBLE
                                        } else {
                                            bookingContainer.visibility = View.VISIBLE
                                        }
                                    } else {
                                        val date = android.text.format.DateFormat
                                            .getDateFormat(context)
                                            .format(appointment.dateAndTime)

                                        val time = android.text.format.DateFormat
                                            .getTimeFormat(context)
                                            .format(appointment.dateAndTime)

                                        val notes = appointment.notes.ifBlank { "No notes" }

                                        appointmentSummary.text =
                                            "Visit with ${chosenPet["name"] ?: "Unknown"}\n\n" +
                                                    "Date: $date\n" +
                                                    "Time: $time\n" +
                                                    "Status: ${appointment.requestStatus}\n\n" +
                                                    "Notes: $notes"

                                        existingContainer.visibility = View.VISIBLE
                                    }
                                }
                            }
                        },
                        onError = { message ->
                            view.post {
                                if (view.tag == true) {
                                    loadingSpinner.visibility = View.GONE
                                    lookupMessage.text = message
                                    lookupMessage.visibility = View.VISIBLE
                                    retryButton.visibility = View.VISIBLE
                                }
                            }
                        }
                    )
                }

                title.text = context.getString(R.string.visit_pet_title, chosenPet["name"]?.toString() ?: "Unknown")

                dateButton.setOnClickListener {
                    val picker = DatePickerDialog(
                        themedContext,
                        { _, year, month, day ->
                            visitDateTime.set(year, month, day)
                            dateSelected = true
                            errorMessage.visibility = View.GONE
                            dateButton.text = android.text.format.DateFormat.getDateFormat(context)
                                .format(visitDateTime.time)
                        },
                        visitDateTime.get(Calendar.YEAR),
                        visitDateTime.get(Calendar.MONTH),
                        visitDateTime.get(Calendar.DAY_OF_MONTH)
                    )
                    picker.datePicker.minDate = System.currentTimeMillis()
                    picker.show()
                }

                timeButton.setOnClickListener {
                    TimePickerDialog(
                        themedContext,
                        { _, hour, minute ->
                            visitDateTime.set(Calendar.HOUR_OF_DAY, hour)
                            visitDateTime.set(Calendar.MINUTE, minute)
                            timeSelected = true
                            errorMessage.visibility = View.GONE
                            timeButton.text = android.text.format.DateFormat.getTimeFormat(context)
                                .format(visitDateTime.time)
                        },
                        visitDateTime.get(Calendar.HOUR_OF_DAY),
                        visitDateTime.get(Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(context)
                    ).show()
                }

                reviewButton.setOnClickListener {
                    if (submitting || submitted || existingAppointment != null) return@setOnClickListener
                    visitDateTime.set(Calendar.SECOND, 0)
                    visitDateTime.set(Calendar.MILLISECOND, 0)
                    val userNotes = notesEditText.text.toString().trim()
                    val error = visitValidationError(
                        dateSelected,
                        timeSelected,
                        visitDateTime.timeInMillis,
                        System.currentTimeMillis()
                    )
                    if (error != null) {
                        errorMessage.setText(error)
                        errorMessage.visibility = View.VISIBLE
                    } else {
                        errorMessage.visibility = View.GONE
                        val date = android.text.format.DateFormat.getDateFormat(context)
                            .format(visitDateTime.time)
                        val time = android.text.format.DateFormat.getTimeFormat(context)
                            .format(visitDateTime.time)
                        val petId = chosenPet["objectId"]?.toString()
                        val userId = MainActivity.loggedInUser?.objectId
                        val submissionError = visitSubmissionError(MainActivity.skipLoginForDevelopment, petId, userId)
                        val notesPreview = if (userNotes.isNotEmpty()) userNotes else "No Notes"
                        val summary = context.getString(
                            R.string.visit_review_summary,
                            chosenPet["name"]?.toString() ?: "Unknown", date, time, notesPreview
                        ) + if (submissionError != null) "\n\n${context.getString(submissionError)}" else ""
                        val dialog = AlertDialog.Builder(themedContext)
                            .setTitle(R.string.visit_review_title)
                            .setMessage(summary)
                            .setPositiveButton(R.string.visit_submit, null)
                            .setNegativeButton(R.string.visit_edit_request, null)
                            .create()
                        view.setTag(R.id.visitReviewButton, dialog)
                        dialog.setOnShowListener {
                            val submitButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            submitButton.isEnabled = submissionError == null
                            submitButton.setOnClickListener submitClick@{
                                if (submitting || submitted) return@submitClick
                                val currentError = visitSubmissionError(
                                    MainActivity.skipLoginForDevelopment,
                                    petId,
                                    MainActivity.loggedInUser?.objectId
                                )
                                if (currentError != null || userId != MainActivity.loggedInUser?.objectId) {
                                    dialog.setMessage(context.getString(currentError ?: R.string.visit_login_required))
                                    return@submitClick
                                }
                                submitting = true
                                submitButton.isEnabled = false
                                submitButton.setText(R.string.visit_submitting)
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                                dialog.setCancelable(false)
                                dateButton.isEnabled = false
                                timeButton.isEnabled = false
                                reviewButton.isEnabled = false
                                backButton.isEnabled = false
                                notesEditText.isEnabled = false
                                AppointmentController().submitVisit(
                                    requestId, petId.orEmpty(), userId.orEmpty(), visitDateTime.timeInMillis, userNotes,
                                    onSuccess = {
                                        view.post {
                                            if (view.tag == true) {
                                                submitting = false
                                                submitted = true
                                                dialog.dismiss()
                                                errorMessage.setText(R.string.visit_saved)
                                                errorMessage.setTextColor(android.graphics.Color.rgb(46, 125, 50))
                                                errorMessage.visibility = View.VISIBLE
                                                backButton.isEnabled = true
                                                checkAppointment()
                                            }
                                        }
                                    },
                                    onError = { message ->
                                        view.post {
                                            if (view.tag == true) {
                                                submitting = false
                                                submitButton.isEnabled = true
                                                submitButton.setText(R.string.visit_submit)
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                                                dialog.setCancelable(true)
                                                dialog.setMessage("$summary\n\n$message")
                                                dateButton.isEnabled = true
                                                timeButton.isEnabled = true
                                                reviewButton.isEnabled = true
                                                backButton.isEnabled = true
                                                notesEditText.isEnabled = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        dialog.show()
                    }
                }

                cancelButton.setOnClickListener {
                    val appointment = existingAppointment
                        ?: return@setOnClickListener

                    AlertDialog.Builder(themedContext)
                        .setTitle("Cancel appointment?")
                        .setMessage("This will delete your visitation appointment.")
                        .setNegativeButton("Keep appointment", null)
                        .setPositiveButton("Cancel appointment") { _, _ ->
                            cancelButton.isEnabled = false

                            AppointmentController().cancelVisit(
                                appointmentId = appointment.objectId,
                                onSuccess = {
                                    view.post {
                                        if (view.tag == true) {
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                onError = { message ->
                                    view.post {
                                        if (view.tag == true) {
                                            cancelButton.isEnabled = true
                                            lookupMessage.text = message
                                            lookupMessage.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            )
                        }
                        .show()
                }

                backButton.setOnClickListener { navController.popBackStack() }
                backButton2.setOnClickListener { navController.popBackStack() }

                retryButton.setOnClickListener { checkAppointment() }
                checkAppointment()
                view
            },
            onRelease = {
                it.tag = false
                (it.getTag(R.id.visitReviewButton) as? AlertDialog)?.dismiss()
            }
        )
    }
}
