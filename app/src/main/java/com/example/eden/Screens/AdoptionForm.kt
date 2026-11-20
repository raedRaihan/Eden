package com.example.eden.Screens

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import android.widget.Button
import android.widget.EditText
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.app.AlertDialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.eden.R
import com.example.eden.uiElements.BottomBar
import com.example.eden.uiElements.TopBar
import android.content.res.ColorStateList
import android.view.View
import android.widget.ProgressBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import com.example.eden.MainActivity
import java.util.Locale
import java.util.UUID

@Composable
fun AdoptionForm(navController: NavController, chosenPet: HashMap<*, *>)
{
    val colors = MaterialTheme.colorScheme

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

                val view = LayoutInflater.from(newContext).inflate(R.layout.adoption_form, null, false)

                val title = view.findViewById<TextView>(R.id.afTemp)
                title.text = context.getString(
                    R.string.adoption_pet_title,
                    chosenPet["name"]?.toString() ?: context.getString(R.string.unknown_pet)
                )

                val backButton = view.findViewById<Button>(R.id.adoptionBackButton)
                val formFields = view.findViewById<LinearLayout>(R.id.adoptionFormFields)
                val housingInput = view.findViewById<Spinner>(R.id.adoptionHousingInput)
                val experienceInput = view.findViewById<Spinner>(R.id.adoptionExperienceInput)
                val otherPetsInput = view.findViewById<Spinner>(R.id.adoptionOtherPetsInput)
                val hoursAloneInput = view.findViewById<Spinner>(R.id.adoptionHoursAloneInput)
                val reasonInput = view.findViewById<EditText>(R.id.adoptionReasonInput)
                val notesInput = view.findViewById<EditText>(R.id.adoptionNotesInput)
                val responsibilityInput = view.findViewById<CheckBox>(R.id.adoptionResponsibilityInput)
                val reviewButton = view.findViewById<Button>(R.id.adoptionReviewButton)

                val loading = view.findViewById<ProgressBar>(R.id.adoptionLoading)
                val statusMessage = view.findViewById<TextView>(R.id.adoptionStatusMessage)
                val controller = AppointmentController()
                val requestId = UUID.randomUUID().toString()
                var submitting = false

                fun configureSpinner(spinner: Spinner, values: List<String>) {
                    spinner.adapter = ArrayAdapter(
                        newContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        values,
                    )
                }
                configureSpinner(housingInput, listOf("Select housing", "Own", "Rent", "Other"))
                configureSpinner(
                    experienceInput,
                    listOf("Select experience", "First-time", "Some experience", "Experienced"),
                )
                configureSpinner(otherPetsInput, listOf("Select an answer", "No", "Yes"))
                configureSpinner(
                    hoursAloneInput,
                    listOf("Select time alone", "0–2 hours", "3–5 hours", "6–8 hours", "8+ hours"),
                )

                loading.indeterminateTintList =
                    ColorStateList.valueOf(colors.primary.toArgb())

                view.tag = true

                fun showApplicationStatus(requestStatus: String) {
                    formFields.visibility = View.GONE

                    val status = requestStatus.trim().uppercase(Locale.ROOT)
                    val messageId = when (status) {
                        "ACCEPTED" -> R.string.adoption_accepted
                        "REJECTED", "DENIED" -> R.string.adoption_rejected
                        else -> R.string.adoption_already_submitted
                    }

                    statusMessage.text = context.getString(messageId) + "\n\n" +
                        context.getString(R.string.adoption_status, status.ifBlank { "Unknown" })
                    statusMessage.visibility = View.VISIBLE
                }

                fun checkApplication() {
                    formFields.visibility = View.GONE
                    statusMessage.visibility = View.GONE
                    loading.visibility = View.VISIBLE

                    val userId = MainActivity.loggedInUser?.objectId
                    val petId = chosenPet["objectId"]?.toString()

                    if (userId.isNullOrBlank() || petId.isNullOrBlank()) {
                        loading.visibility = View.GONE
                        statusMessage.setText(R.string.visit_lookup_identity_required)
                        statusMessage.visibility = View.VISIBLE
                        return
                    }

                    controller.findAdoptionApplication(
                        userId = userId,
                        petId = petId,
                        onSuccess = { application ->
                            view.post {
                                if (view.tag == true) {
                                    loading.visibility = View.GONE

                                    if (application == null) {
                                        if (chosenPet["status"] == "Available") {
                                            formFields.visibility = View.VISIBLE
                                        } else {
                                            statusMessage.setText(R.string.adoption_unavailable)
                                            statusMessage.visibility = View.VISIBLE
                                        }
                                    } else {
                                        showApplicationStatus(application.requestStatus)
                                    }
                                }
                            }
                        },
                        onError = { message ->
                            view.post {
                                if (view.tag == true) {
                                    loading.visibility = View.GONE
                                    statusMessage.text = message
                                    statusMessage.visibility = View.VISIBLE
                                }
                            }
                        }
                    )
                }

                backButton.setOnClickListener {
                    navController.popBackStack()
                }
                reviewButton.setOnClickListener {
                    if (submitting) return@setOnClickListener
                    val housing = housingInput.selectedItem?.toString().orEmpty()
                    val experience = experienceInput.selectedItem?.toString().orEmpty()
                    val hasOtherPets = otherPetsInput.selectedItem?.toString().orEmpty()
                    val hoursAlone = hoursAloneInput.selectedItem?.toString().orEmpty()
                    val applicationText = reasonInput.text.toString().trim()
                    val notes = notesInput.text.toString().trim()

                    if (housingInput.selectedItemPosition == 0) {
                        statusMessage.text = "Select your housing type."
                        statusMessage.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                    if (experienceInput.selectedItemPosition == 0) {
                        statusMessage.text = "Select your pet-care experience."
                        statusMessage.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                    if (otherPetsInput.selectedItemPosition == 0) {
                        statusMessage.text = "Tell us whether you have other pets."
                        statusMessage.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                    if (hoursAloneInput.selectedItemPosition == 0) {
                        statusMessage.text = "Select how long the pet may be alone."
                        statusMessage.visibility = View.VISIBLE
                        return@setOnClickListener
                    }

                    if (applicationText.isBlank()) {
                        reasonInput.error = context.getString(
                            R.string.adoption_application_required
                        )
                        reasonInput.requestFocus()
                        return@setOnClickListener
                    }
                    if (!responsibilityInput.isChecked) {
                        statusMessage.text = "Accept the responsibility statement before continuing."
                        statusMessage.visibility = View.VISIBLE
                        return@setOnClickListener
                    }

                    reasonInput.error = null
                    statusMessage.visibility = View.GONE

                    val summary = "Housing: $housing\n" +
                        "Experience: $experience\n" +
                        "Other pets: $hasOtherPets\n" +
                        "Time alone: $hoursAlone\n\n" +
                        "Why this pet: $applicationText" +
                        if (notes.isBlank()) "" else "\n\nAdditional notes: $notes"

                    AlertDialog.Builder(newContext)
                        .setTitle(R.string.visit_review_title)
                        .setMessage(summary)
                        .setNegativeButton(R.string.adoption_back_to_form, null)
                        .setPositiveButton(R.string.adoption_submit) { _, _ ->
                            val userId = MainActivity.loggedInUser?.objectId
                            val petId = chosenPet["objectId"]?.toString()

                            if (userId.isNullOrBlank() || petId.isNullOrBlank()) {
                                checkApplication()
                                return@setPositiveButton
                            }

                            submitting = true
                            formFields.isEnabled = false
                            reviewButton.isEnabled = false
                            backButton.isEnabled = false
                            statusMessage.visibility = View.GONE
                            loading.visibility = View.VISIBLE

                            controller.submitAdoption(
                                requestId = requestId,
                                petId = petId,
                                userId = userId,
                                housingType = housing,
                                experienceLevel = experience,
                                hasOtherPets = hasOtherPets == "Yes",
                                hoursAloneRange = hoursAlone,
                                adoptionReason = applicationText,
                                responsibilityAccepted = responsibilityInput.isChecked,
                                notes = notes,
                                onSuccess = {
                                    view.post {
                                        if (view.tag == true) {
                                            submitting = false
                                            loading.visibility = View.GONE
                                            formFields.isEnabled = true
                                            backButton.isEnabled = true
                                            showApplicationStatus("PENDING")
                                        }
                                    }
                                },
                                onError = { message ->
                                    view.post {
                                        if (view.tag == true) {
                                            submitting = false
                                            loading.visibility = View.GONE
                                            formFields.isEnabled = true
                                            reviewButton.isEnabled = true
                                            backButton.isEnabled = true
                                            statusMessage.text = message
                                            statusMessage.visibility = View.VISIBLE
                                        }
                                    }
                                }
                            )
                        }
                        .show()
                        .also { view.setTag(R.id.adoptionReviewButton, it) }
                }
                checkApplication()
                view
            },
            onRelease = {
                it.tag = false
                (it.getTag(R.id.adoptionReviewButton) as? AlertDialog)?.dismiss()
            }
        )

    }
}
