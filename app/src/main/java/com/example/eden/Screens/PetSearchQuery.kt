package com.example.eden.Screens

internal fun petSearchWhereClause(
    name: String,
    minAge: String,
    maxAge: String,
    species: String,
    breed: String,
    size: String,
    gender: String
): String {
    val clauses = mutableListOf<String>()
    fun match(column: String, value: String) {
        clauses.add("$column = '${value.replace("'", "''")}'")
    }
    if (name.isNotEmpty()) match("name", name)
    if (species != "None") {
        match("species", species)
        if (breed != "None") match("breed", breed)
    }
    if (size != "None") match("size", size)
    if (gender != "None") match("gender", gender)
    minAge.toIntOrNull()?.let { clauses.add("ageMonths >= $it") }
    maxAge.toIntOrNull()?.let { clauses.add("ageMonths <= $it") }
    return clauses.joinToString(" AND ")
}
