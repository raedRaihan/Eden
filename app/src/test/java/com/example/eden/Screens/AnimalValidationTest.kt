package com.example.eden.Screens

import org.junit.Assert.*
import org.junit.Test

class AnimalValidationTest {
    @Test fun createRequiresName() {
        assertEquals("Name is required.", createAnimalValidationError(" ", "12", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createRequiresSpecies() {
        assertEquals("Species is required.", createAnimalValidationError("Bella", "12", " ", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createRequiresBreed() {
        assertEquals("Breed is required.", createAnimalValidationError("Bella", "12", "Dog", " ", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createRequiresSize() {
        assertEquals("Size is required.", createAnimalValidationError("Bella", "12", "Dog", "Beagle", " ", "Female", "Friendly", "Eden"))
    }

    @Test fun createRequiresGender() {
        assertEquals("Gender is required.", createAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", " ", "Friendly", "Eden"))
    }

    @Test fun createRequiresDescription() {
        assertEquals("Description is required.", createAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", "Female", " ", "Eden"))
    }

    @Test fun createRequiresShelter() {
        assertEquals("Shelter name is required.", createAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", "Female", "Friendly", " "))
    }

    @Test fun createRejectsInvalidAge() {
        assertEquals("Age must be a non-negative number of months.", createAnimalValidationError("Bella", "abc", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createRejectsNegativeAge() {
        assertEquals("Age must be a non-negative number of months.", createAnimalValidationError("Bella", "-1", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createAcceptsNewborn() {
        assertNull(createAnimalValidationError("Bella", "0", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun createAcceptsFractionalMonths() {
        assertNull(createAnimalValidationError("Bella", "1.5", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden"))
    }

    @Test fun editRequiresName() {
        assertEquals("Name is required.", editAnimalValidationError(" ", "12", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editRequiresSpecies() {
        assertEquals("Species is required.", editAnimalValidationError("Bella", "12", " ", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editRequiresBreed() {
        assertEquals("Breed is required.", editAnimalValidationError("Bella", "12", "Dog", " ", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editRequiresSize() {
        assertEquals("Size is required.", editAnimalValidationError("Bella", "12", "Dog", "Beagle", " ", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editRequiresGender() {
        assertEquals("Gender is required.", editAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", " ", "Friendly", "Eden", "Available"))
    }

    @Test fun editRequiresDescription() {
        assertEquals("Description is required.", editAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", "Female", " ", "Eden", "Available"))
    }

    @Test fun editRequiresShelter() {
        assertEquals("Shelter name is required.", editAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", "Female", "Friendly", " ", "Available"))
    }

    @Test fun editRequiresStatus() {
        assertEquals("Status is required.", editAnimalValidationError("Bella", "12", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", " "))
    }

    @Test fun editRejectsInvalidAge() {
        assertEquals("Age must be a non-negative number.", editAnimalValidationError("Bella", "abc", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editRejectsNegativeAge() {
        assertEquals("Age must be a non-negative number.", editAnimalValidationError("Bella", "-1", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editAcceptsNewborn() {
        assertNull(editAnimalValidationError("Bella", "0", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun editAcceptsFractionalMonths() {
        assertNull(editAnimalValidationError("Bella", "1.5", "Dog", "Beagle", "Medium", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun rejectsNaNOnCreate() {
        assertEquals("Age must be a non-negative number of months.", createAnimalValidationError("Bella", "NaN", "Dog", "Beagle", "Small", "Female", "Friendly", "Eden"))
    }

    @Test fun rejectsInfinityOnCreate() {
        assertEquals("Age must be a non-negative number of months.", createAnimalValidationError("Bella", "Infinity", "Dog", "Beagle", "Small", "Female", "Friendly", "Eden"))
    }

    @Test fun rejectsNaNOnEdit() {
        assertEquals("Age must be a non-negative number.", editAnimalValidationError("Bella", "NaN", "Dog", "Beagle", "Small", "Female", "Friendly", "Eden", "Available"))
    }

    @Test fun rejectsInfinityOnEdit() {
        assertEquals("Age must be a non-negative number.", editAnimalValidationError("Bella", "Infinity", "Dog", "Beagle", "Small", "Female", "Friendly", "Eden", "Available"))
    }

}
