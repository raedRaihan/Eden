package com.example.eden.users

import org.junit.Assert.*
import org.junit.Test

class PetFilterOptionsTest {
    @Test fun removesWhitespaceDuplicatesAndMissingValuesAndSorts() {
        val records = listOf(
            mapOf("species" to " Dog ", "breed" to "Beagle", "size" to "Medium", "gender" to "Female"),
            mapOf("species" to "Cat", "breed" to "Tabby", "size" to "Small", "gender" to "Male"),
            mapOf("species" to "Dog", "breed" to "Beagle"),
            mapOf("species" to " ", "breed" to null)
        )
        val result = petFilterOptions(records)
        assertEquals(listOf("Cat", "Dog"), result.species)
        assertEquals(listOf("Beagle", "Tabby"), result.breeds)
        assertEquals(listOf("Medium", "Small"), result.sizes)
        assertEquals(listOf("Female", "Male"), result.genders)
    }

    @Test fun emptyRecordsGiveEmptyOptions() {
        val result = petFilterOptions(emptyList())
        assertTrue(result.species.isEmpty())
        assertTrue(result.breeds.isEmpty())
        assertTrue(result.sizes.isEmpty())
        assertTrue(result.genders.isEmpty())
    }
}
