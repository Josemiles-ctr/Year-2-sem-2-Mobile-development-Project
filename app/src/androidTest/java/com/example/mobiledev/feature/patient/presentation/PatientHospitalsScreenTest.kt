package com.example.mobiledev.feature.patient.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.repository.ResQRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class PatientHospitalsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun patientHospitalsScreen_rendersHospitalCards() {
        val viewModel = PatientHospitalsViewModel(FakeResQRepository(sampleHospitals()))

        composeRule.setContent {
            PatientHospitalsScreen(viewModel = viewModel)
        }

        composeRule.onNodeWithTag("hospitalCard_H1").assertIsDisplayed()
        composeRule.onNodeWithTag("hospitalCard_H2").assertIsDisplayed()
        composeRule.onNodeWithText("City Central Hospital").assertIsDisplayed()
    }

    @Test
    fun patientHospitalsScreen_loadsMoreHospitalsWhenScrolledToBottom() {
        val viewModel = PatientHospitalsViewModel(FakeResQRepository(sampleHospitals(count = 8)))

        composeRule.setContent {
            PatientHospitalsScreen(viewModel = viewModel)
        }

        composeRule.onNodeWithTag("hospitalCard_H6").performScrollTo()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithTag("hospitalCard_H7").assertCountEquals(1)
        composeRule.onAllNodesWithTag("hospitalCard_H8").assertCountEquals(1)
    }

    private fun sampleHospitals(count: Int = 6): List<HospitalEntity> {
        return (1..count).map { index ->
            HospitalEntity(
                id = "H$index",
                adminId = "A$index",
                name = "Hospital $index",
                email = "hospital$index@example.com",
                phone = "555-01${index.toString().padStart(2, '0')}",
                location = "$index Main Street",
                latitude = 0.0,
                longitude = index.toDouble(),
                uuid = "UUID-$index",
                passwordHash = "hash",
                status = HospitalStatus.APPROVED,
                activeAmbulances = index,
                createdAt = 1L,
                updatedAt = 1L
            )
        }
    }

    private class FakeResQRepository(
        private val hospitals: List<HospitalEntity>
    ) : ResQRepository {
        override fun getAllUsersStream(): Flow<List<UserEntity>> = flowOf(emptyList())
        override suspend fun getUserById(id: String): UserEntity? = null
        override suspend fun getUserByEmail(email: String): UserEntity? = null
        override suspend fun insertUser(user: UserEntity) = Unit
        override suspend fun updateUser(user: UserEntity) = Unit
        override suspend fun deleteUser(user: UserEntity) = Unit

        override fun getAllHospitalsStream(): Flow<List<HospitalEntity>> = flowOf(hospitals)
        override fun getApprovedHospitalsStream(): Flow<List<HospitalEntity>> = flowOf(hospitals)
        override suspend fun getHospitalById(id: String): HospitalEntity? = hospitals.firstOrNull { it.id == id }
        override suspend fun getHospitalByAdminId(adminId: String): HospitalEntity? = hospitals.firstOrNull { it.adminId == adminId }
        override suspend fun insertHospital(hospital: HospitalEntity) = Unit
        override suspend fun updateHospital(hospital: HospitalEntity) = Unit
        override suspend fun deleteHospital(hospital: HospitalEntity) = Unit
        override suspend fun loginHospital(email: String, password: String): HospitalEntity? = null

        override fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>> = flowOf(emptyList())
        override fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>> = flowOf(emptyList())
        override fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>> = flowOf(emptyList())
        override suspend fun getAmbulanceById(id: String): AmbulanceEntity? = null
        override suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity? = null
        override suspend fun insertAmbulance(ambulance: AmbulanceEntity) = Unit
        override suspend fun updateAmbulance(ambulance: AmbulanceEntity) = Unit
        override suspend fun deleteAmbulance(ambulance: AmbulanceEntity) = Unit

        override fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override suspend fun getRequestById(id: String): EmergencyRequestEntity? = null
        override suspend fun insertRequest(request: EmergencyRequestEntity) = Unit
        override suspend fun updateRequest(request: EmergencyRequestEntity) = Unit
        override suspend fun deleteRequest(request: EmergencyRequestEntity) = Unit
        override suspend fun softDeleteRequest(id: String) = Unit
    }
}



