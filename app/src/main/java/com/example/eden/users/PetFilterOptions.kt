package com.example.eden.users

internal fun petFilterOptions(records: List<Map<*, *>>): PetFilterOptions {
    fun values(key: String): List<String> = records.mapNotNull {
        it[key]?.toString()?.trim()?.takeIf(String::isNotEmpty)
    }.distinct().sorted()

    return PetFilterOptions(
        species = values("species"),
        breeds = values("breed"),
        sizes = values("size"),
        genders = values("gender")
    )
}
