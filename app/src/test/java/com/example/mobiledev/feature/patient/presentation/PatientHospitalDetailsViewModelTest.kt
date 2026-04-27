package com.example.mobiledev.feature.patient.presentation

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PatientHospitalDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `offline state is true when all ambulances are offline`() = runTest {
        val repository = FakeResQRepository(
            hospital = sampleHospital(),
            ambulances = listOf(
                sampleAmbulance(id = "AMB_1", status = "OFFLINE"),
                sampleAmbulance(id = "AMB_2", status = "OFFLINE")
            )
        )
        val session = AuthSessionManager().apply {
            setPrincipal(AuthPrincipal(userId = "PATIENT_1", role = AppRole.PATIENT))
        }

        val viewModel = PatientHospitalDetailsViewModel(
            repository = repository,
            hospitalId = "HOSPITAL_1",
            authSessionManager = session,
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isHospitalOffline)
        assertEquals(0, viewModel.uiState.value.availableAmbulanceCount)
    }

    @Test
    fun `submitEmergencyRequest inserts pending request`() = runTest {
        val repository = FakeResQRepository(
            hospital = sampleHospital(),
            ambulances = listOf(sampleAmbulance(status = "AVAILABLE"))
        )
        val session = AuthSessionManager().apply {
            setPrincipal(AuthPrincipal(userId = "PATIENT_1", role = AppRole.PATIENT))
        }

        val viewModel = PatientHospitalDetailsViewModel(
            repository = repository,
            hospitalId = "HOSPITAL_1",
            authSessionManager = session,
        )

        advanceUntilIdle()
        viewModel.submitEmergencyRequest("Chest pain and breathing difficulty")
        advanceUntilIdle()

        assertEquals(1, repository.insertedRequests.size)
        assertEquals("PENDING", repository.insertedRequests.first().status)
        assertNotNull(viewModel.uiState.value.submitSuccessMessage)
        assertEquals(null, viewModel.uiState.value.submitErrorMessage)
    }

    @Test
    fun `submitEmergencyRequest rejects blank description`() = runTest {
        val repository = FakeResQRepository(
            hospital = sampleHospital(),
            ambulances = listOf(sampleAmbulance(status = "AVAILABLE"))
        )
        val session = AuthSessionManager().apply {
            setPrincipal(AuthPrincipal(userId = "PATIENT_1", role = AppRole.PATIENT))
        }

        val viewModel = PatientHospitalDetailsViewModel(
            repository = repository,
            hospitalId = "HOSPITAL_1",
            authSessionManager = session,
        )

        advanceUntilIdle()
        viewModel.submitEmergencyRequest("  ")
        advanceUntilIdle()

        assertTrue(repository.insertedRequests.isEmpty())
        assertFalse(viewModel.uiState.value.submitErrorMessage.isNullOrBlank())
    }

    private fun sampleHospital() = HospitalEntity(
        id = "HOSPITAL_1",
        adminId = "ADMIN_1",
        name = "City Central Hospital",
        email = "hospital@example.com",
        phone = "555-0100",
        location = "123 Healthcare Ave",
        latitude = 40.7128,
        longitude = -74.0060,
        uuid = "UUID_HOSPITAL_1",
        passwordHash = "hash",
        status = HospitalStatus.APPROVED,
        activeAmbulances = 2,
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun sampleAmbulance(
        id: String = "AMB_1",
        status: String
    ) = AmbulanceEntity(
        id = id,
        hospitalId = "HOSPITAL_1",
        driverId = "DRIVER_1",
        registrationNo = "REG-1001",
        licenseNo = "LIC-1001",
        status = status,
        latitude = 40.71,
        longitude = -74.00,
        createdAt = 1L,
        updatedAt = 1L
    )

    private class FakeResQRepository(
        hospital: HospitalEntity,
        ambulances: List<AmbulanceEntity>
    ) : ResQRepository {

        private val hospitalFlow = MutableStateFlow(hospital)
        private val ambulanceFlow = MutableStateFlow(ambulances)
        val insertedRequests = mutableListOf<EmergencyRequestEntity>()

        override fun getAllUsersStream(): Flow<List<UserEntity>> = flowOf(emptyList())
        override suspend fun getUserById(id: String): UserEntity? = null
        override suspend fun getUserByEmail(email: String): UserEntity? = null
        override suspend fun insertUser(user: UserEntity) = Unit
        override suspend fun updateUser(user: UserEntity) = Unit
        override suspend fun deleteUser(user: UserEntity) = Unit

        override fun getAllHospitalsStream(): Flow<List<HospitalEntity>> = flowOf(listOf(hospitalFlow.value))
        override fun getApprovedHospitalsStream(): Flow<List<HospitalEntity>> = flowOf(listOf(hospitalFlow.value))
        override suspend fun getHospitalById(id: String): HospitalEntity? = hospitalFlow.value.takeIf { it.id == id }
        override suspend fun getHospitalByAdminId(adminId: String): HospitalEntity? = null
        override suspend fun insertHospital(hospital: HospitalEntity) = Unit
        override suspend fun updateHospital(hospital: HospitalEntity) {
            hospitalFlow.value = hospital
        }
        override suspend fun deleteHospital(hospital: HospitalEntity) = Unit
        override suspend fun loginHospital(email: String, password: String): HospitalEntity? = null

        override fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>> = flowOf(ambulanceFlow.value)
        override fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>> =
            flowOf(ambulanceFlow.value.filter { it.hospitalId == hospitalId })
        override fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>> =
            flowOf(ambulanceFlow.value.filter { it.hospitalId == hospitalId && it.status == "AVAILABLE" })
        override suspend fun getAmbulanceById(id: String): AmbulanceEntity? =
            ambulanceFlow.value.firstOrNull { it.id == id }
        override suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity? =
            ambulanceFlow.value.firstOrNull { it.driverId == driverId }
        override suspend fun insertAmbulance(ambulance: AmbulanceEntity) = Unit
        override suspend fun updateAmbulance(ambulance: AmbulanceEntity) = Unit
        override suspend fun deleteAmbulance(ambulance: AmbulanceEntity) = Unit

        override fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>> = flowOf(emptyList())
        override suspend fun getRequestById(id: String): EmergencyRequestEntity? =
            insertedRequests.firstOrNull { it.id == id }
        override suspend fun insertRequest(request: EmergencyRequestEntity) {
            insertedRequests += request
        }
        override suspend fun updateRequest(request: EmergencyRequestEntity) = Unit
        override suspend fun deleteRequest(request: EmergencyRequestEntity) = Unit
        override suspend fun softDeleteRequest(id: String) = Unit
    }
}



