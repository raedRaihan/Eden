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
