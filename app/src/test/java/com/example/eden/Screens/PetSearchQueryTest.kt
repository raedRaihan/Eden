package com.example.eden.Screens

import org.junit.Assert.assertEquals
import org.junit.Test

class PetSearchQueryTest {
    private fun query(
        name: String = "", min: String = "", max: String = "",
        species: String = "None", breed: String = "None",
        size: String = "None", gender: String = "None"
    ) = petSearchWhereClause(name, min, max, species, breed, size, gender)

    @Test fun noFiltersMeansNoWhereClause() {
        assertEquals("", query())
    }

    @Test fun includesAllSelectedFilters() {
        assertEquals(
            "name = 'Bella' AND species = 'Dog' AND breed = 'Beagle' AND size = 'Small' AND gender = 'Female' AND ageMonths >= 0 AND ageMonths <= 12",
            query("Bella", "0", "12", "Dog", "Beagle", "Small", "Female")
        )
    }

    @Test fun ignoresBreedWithoutSpecies() {
        assertEquals("", query(breed = "Beagle"))
    }

    @Test fun supportsOnlyMaximumAge() {
        assertEquals("ageMonths <= 12", query(max = "12"))
    }

    @Test fun supportsOnlyMinimumAge() {
        assertEquals("ageMonths >= 0", query(min = "0"))
    }

    @Test fun ignoresNonNumericAgeFilters() {
        assertEquals("species = 'Dog'", query(min = "abc", max = "1.5", species = "Dog"))
    }

    @Test fun escapesApostropheInPetName() {
        assertEquals("name = 'O''Malley'", query(name = "O'Malley"))
    }

    @Test fun escapesApostropheInBreed() {
        assertEquals("species = 'Dog' AND breed = 'D''Example'", query(species = "Dog", breed = "D'Example"))
    }

    @Test fun treatsQueryCharactersAsNameData() {
        assertEquals("name = ''' OR 1=1 --'", query(name = "' OR 1=1 --"))
    }
}
