package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.model.Ambulance
import com.example.mobiledev.data.model.AmbulanceStatus
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class LocalEmergencyRepository(
    private val resQRepository: ResQRepository,
    private val authSessionManager: AuthSessionManager
) : EmergencyRepository {

    override fun getEmergencyRequests(
        status: String?,
        dateFrom: Long?,
        dateTo: Long?,
        limit: Int,
        offset: Int
    ): Flow<List<EmergencyRequest>> {
        val principal = authSessionManager.currentPrincipal

        val requestFlow: Flow<List<EmergencyRequestEntity>> = when (principal.role) {
            AppRole.PATIENT -> {
                val userId = principal.userId
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    resQRepository.getRequestsByUserStream(userId)
                }
            }
            AppRole.DRIVER, AppRole.HOSPITAL_ADMIN, AppRole.SYSTEM_ADMIN -> {
                resQRepository.getAllActiveRequestsStream()
            }
            AppRole.GUEST -> flowOf(emptyList())
        }

        return requestFlow.map { requests ->
            mapAndFilterRequests(
                requests = requests,
                status = status,
                dateFrom = dateFrom,
                dateTo = dateTo,
                limit = limit,
                offset = offset
            )
        }
    }

    override fun getAmbulances(): Flow<List<Ambulance>> {
        val principal = authSessionManager.currentPrincipal
        return when (principal.role) {
            AppRole.PATIENT, AppRole.GUEST -> flowOf(emptyList())
            else -> resQRepository.getAllAmbulancesStream().map { ambulances ->
                ambulances.map { entity ->
                    Ambulance(
                        id = entity.id,
                        plateNumber = entity.registrationNo,
                        driverName = entity.driverId,
                        status = mapAmbulanceStatus(entity.status),
                        currentEmergencyId = null
                    )
                }
            }
        }
    }

    override suspend fun updateEmergencyStatus(
        id: String,
        status: EmergencyStatus
    ): Result<Unit> {
        return try {
            val request = resQRepository.getRequestById(id)
            if (request != null) {
                val updatedRequest = request.copy(
                    status = status.name,
                    updatedAt = System.currentTimeMillis()
                )
                resQRepository.updateRequest(updatedRequest)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Request not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignAmbulance(
        requestId: String,
        ambulanceId: String
    ): Result<Unit> {
        return try {
            val request = resQRepository.getRequestById(requestId)
            val ambulance = resQRepository.getAmbulanceById(ambulanceId)

            if (request != null && ambulance != null) {
                val updatedRequest = request.copy(
                    ambulanceId = ambulanceId,
                    status = "ASSIGNED",
                    updatedAt = System.currentTimeMillis()
                )
                val updatedAmbulance = ambulance.copy(
                    status = "ON_EMERGENCY",
                    updatedAt = System.currentTimeMillis()
                )

                resQRepository.updateRequest(updatedRequest)
                resQRepository.updateAmbulance(updatedAmbulance)

                Result.success(Unit)
            } else {
                Result.failure(Exception("Request or ambulance not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapAndFilterRequests(
        requests: List<EmergencyRequestEntity>,
        status: String?,
        dateFrom: Long?,
        dateTo: Long?,
        limit: Int,
        offset: Int
    ): List<EmergencyRequest> {
        var filtered = requests

        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }

        if (dateFrom != null) {
            filtered = filtered.filter { it.createdAt >= dateFrom }
        }
        if (dateTo != null) {
            filtered = filtered.filter { it.createdAt <= dateTo }
        }

        filtered = filtered
            .sortedByDescending { it.createdAt }
            .drop(offset)
            .take(limit)

        return filtered.map { entity ->
            EmergencyRequest(
                id = entity.id,
                patientName = entity.description,
                location = entity.location,
                phoneNumber = "",
                description = entity.description,
                status = mapStatus(entity.status),
                timestamp = entity.createdAt,
                assignedAmbulanceId = entity.ambulanceId
            )
        }
    }

    private fun mapStatus(status: String): EmergencyStatus {
        return when (status.uppercase()) {
            "PENDING" -> EmergencyStatus.PENDING
            "ASSIGNED" -> EmergencyStatus.ASSIGNED
            "ON_WAY" -> EmergencyStatus.EN_ROUTE
            "ARRIVED" -> EmergencyStatus.ARRIVED
            "COMPLETED" -> EmergencyStatus.COMPLETED
            "CANCELLED" -> EmergencyStatus.CANCELLED
            else -> EmergencyStatus.PENDING
        }
    }

    private fun mapAmbulanceStatus(status: String): AmbulanceStatus {
        return when (status.uppercase()) {
            "AVAILABLE" -> AmbulanceStatus.AVAILABLE
            "ON_EMERGENCY", "BUSY" -> AmbulanceStatus.ON_MISSION
            "OFFLINE" -> AmbulanceStatus.MAINTENANCE
            else -> AmbulanceStatus.AVAILABLE
        }
    }
}