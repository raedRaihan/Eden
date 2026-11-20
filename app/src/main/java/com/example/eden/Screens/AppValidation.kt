package com.example.eden.Screens

internal fun signUpValidationError(
    first: String, last: String, email: String, password: String, confirmPassword: String
): String? {
    return when {
        first.isBlank() || last.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank() ->
            "All fields are required."
        !email.contains("@") -> "Enter a valid email address."
        password != confirmPassword -> "Passwords do not match."
        else -> null
    }
}

internal fun preferencesValidationError(minAge: String, maxAge: String, loggedIn: Boolean): String? {
    val minimum = minAge.takeIf { it.isNotBlank() }?.toIntOrNull()
    val maximum = maxAge.takeIf { it.isNotBlank() }?.toIntOrNull()
    return when {
        minAge.isNotBlank() && minimum == null -> "Minimum age must be a whole number."
        maxAge.isNotBlank() && maximum == null -> "Maximum age must be a whole number."
        minimum != null && minimum < 0 -> "Minimum age cannot be negative."
        maximum != null && maximum < 0 -> "Maximum age cannot be negative."
        minimum != null && maximum != null && minimum > maximum ->
            "Minimum age cannot be greater than maximum age."
        !loggedIn -> "Log in before saving preferences."
        else -> null
    }
}

internal fun profileValidationError(
    loggedIn: Boolean,
    first: String,
    last: String,
    validEmail: Boolean,
    profilePassword: String,
    validPhone: Boolean = true,
): String? {
    return when {
        !loggedIn -> "Log in before changing account settings."
        first.isBlank() -> "First name is required."
        last.isBlank() -> "Last name is required."
        !validEmail -> "Enter a valid email address."
        !validPhone -> "Enter a valid phone number or leave it blank."
        profilePassword.isBlank() -> "Enter your current password to save changes."
        else -> null
    }
}

internal fun passwordValidationError(
    loggedIn: Boolean, isPasswordAccount: Boolean, currentPassword: String,
    newPassword: String, confirmPassword: String
): String? {
    return when {
        !loggedIn -> "Log in before changing your password."
        !isPasswordAccount -> "Password changes are unavailable for social accounts."
        currentPassword.isBlank() -> "Enter your current password."
        newPassword.length < 8 -> "The new password must contain at least 8 characters."
        newPassword != confirmPassword -> "The new passwords do not match."
        currentPassword == newPassword -> "Choose a password different from the current one."
        else -> null
    }
}

internal fun createAnimalValidationError(
    name: String, ageMonths: String, species: String, breed: String, size: String,
    gender: String, description: String, shelterName: String
): String? {
    val parsedAge = ageMonths.toDoubleOrNull()
    return when {
        name.isBlank() -> "Name is required."
        parsedAge == null || !parsedAge.isFinite() || parsedAge < 0 -> "Age must be a non-negative number of months."
        species.isBlank() -> "Species is required."
        breed.isBlank() -> "Breed is required."
        size.isBlank() -> "Size is required."
        gender.isBlank() -> "Gender is required."
        description.isBlank() -> "Description is required."
        shelterName.isBlank() -> "Shelter name is required."
        else -> null
    }
}

internal fun editAnimalValidationError(
    name: String, ageMonths: String, species: String, breed: String, size: String,
    gender: String, description: String, shelterName: String, status: String
): String? {
    val age = ageMonths.toDoubleOrNull()
    return when {
        name.isBlank() -> "Name is required."
        age == null || !age.isFinite() || age < 0 -> "Age must be a non-negative number."
        species.isBlank() -> "Species is required."
        breed.isBlank() -> "Breed is required."
        size.isBlank() -> "Size is required."
        gender.isBlank() -> "Gender is required."
        description.isBlank() -> "Description is required."
        shelterName.isBlank() -> "Shelter name is required."
        status.isBlank() -> "Status is required."
        else -> null
    }
}

internal fun canSubmitLogin(email: String, password: String, loading: Boolean): Boolean =
    !loading && email.isNotBlank() && password.isNotBlank()

internal fun adoptionSubmissionError(
    requestId: String, petId: String, userId: String, notes: String
): String? = when {
    requestId.isBlank() || petId.isBlank() || userId.isBlank() ->
        "Sign in and select a pet before applying."
    notes.trim().isBlank() || notes.trim().length > 500 ->
        "Write an application between 1 and 500 characters."
    else -> null
}
