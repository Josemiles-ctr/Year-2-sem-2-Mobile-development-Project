package com.example.mobiledev.feature.patient.presentation

import com.example.mobiledev.data.location.Coordinates
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PatientHospitalFiltersTest {

    @Test
    fun `filterHospitalsByQuery returns all hospitals for blank query`() {
        val hospitals = sampleHospitals()

        val filtered = filterHospitalsByQuery(hospitals, "   ")

        assertEquals(hospitals, filtered)
    }

    @Test
    fun `filterHospitalsByQuery matches hospital name case insensitively`() {
        val hospitals = sampleHospitals()

        val filtered = filterHospitalsByQuery(hospitals, "central")

        assertEquals(listOf(hospitals[0]), filtered)
    }

    @Test
    fun `filterHospitalsByQuery matches hospital location`() {
        val hospitals = sampleHospitals()

        val filtered = filterHospitalsByQuery(hospitals, "north street")

        assertEquals(listOf(hospitals[1]), filtered)
    }

    @Test
    fun `filterHospitalsByQuery matches hospital phone`() {
        val hospitals = sampleHospitals()

        val filtered = filterHospitalsByQuery(hospitals, "555-0102")

        assertEquals(listOf(hospitals[2]), filtered)
    }

    @Test
    fun `filterHospitalsByQuery returns empty list when nothing matches`() {
        val hospitals = sampleHospitals()

        val filtered = filterHospitalsByQuery(hospitals, "unknown")

        assertEquals(emptyList<HospitalEntity>(), filtered)
    }

    @Test
    fun `sortHospitalsByDistance returns original order when location is null`() {
        val hospitals = sampleHospitals()

        val sorted = sortHospitalsByDistance(hospitals, null)

        assertEquals(hospitals, sorted)
    }

    @Test
    fun `sortHospitalsByDistance orders by nearest hospital first`() {
        val hospitals = sampleHospitals()

        val sorted = sortHospitalsByDistance(
            hospitals,
            Coordinates(latitude = 0.0, longitude = 1.5)
        )

        assertEquals(listOf(hospitals[1], hospitals[2], hospitals[0]), sorted)
    }

    private fun sampleHospitals(): List<HospitalEntity> = listOf(
        HospitalEntity(
            id = "H1",
            adminId = "A1",
            name = "City Central Hospital",
            email = "central@example.com",
            phone = "555-0101",
            location = "123 Healthcare Ave",
            latitude = 0.0,
            longitude = 0.0,
            uuid = "UUID-1",
            passwordHash = "hash",
            status = HospitalStatus.APPROVED,
            activeAmbulances = 2,
            createdAt = 1L,
            updatedAt = 1L
        ),
        HospitalEntity(
            id = "H2",
            adminId = "A2",
            name = "Northside Medical",
            email = "north@example.com",
            phone = "555-0102",
            location = "78 North Street",
            latitude = 0.0,
            longitude = 1.0,
            uuid = "UUID-2",
            passwordHash = "hash",
            status = HospitalStatus.APPROVED,
            activeAmbulances = 1,
            createdAt = 1L,
            updatedAt = 1L
        ),
        HospitalEntity(
            id = "H3",
            adminId = "A3",
            name = "West End General",
            email = "west@example.com",
            phone = "555-0103",
            location = "91 West End Blvd",
            latitude = 0.0,
            longitude = 2.0,
            uuid = "UUID-3",
            passwordHash = "hash",
            status = HospitalStatus.APPROVED,
            activeAmbulances = 0,
            createdAt = 1L,
            updatedAt = 1L
        )
    )
}



