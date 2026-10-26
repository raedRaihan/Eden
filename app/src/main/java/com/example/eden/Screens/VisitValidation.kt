package com.example.eden.Screens

import com.example.eden.R

internal fun visitValidationError(
    dateSelected: Boolean,
    timeSelected: Boolean,
    visitTimeMillis: Long,
    nowMillis: Long
): Int? = when {
    !dateSelected -> R.string.visit_date_required
    !timeSelected -> R.string.visit_time_required
    visitTimeMillis <= nowMillis -> R.string.visit_future_required
    else -> null
}

internal fun visitSubmissionError(previewMode: Boolean, petId: String?, userId: String?): Int? = when {
    previewMode -> R.string.visit_preview_only
    userId.isNullOrBlank() -> R.string.visit_login_required
    petId.isNullOrBlank() -> R.string.visit_pet_required
    else -> null
}
