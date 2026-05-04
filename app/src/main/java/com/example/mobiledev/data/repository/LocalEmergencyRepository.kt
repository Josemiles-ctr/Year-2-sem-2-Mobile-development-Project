package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.NotificationEntity
import com.example.mobiledev.data.model.Ambulance
import com.example.mobiledev.data.model.AmbulanceStatus
import com.example.mobiledev.data.model.EmergencyPriority
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

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
        val hospitalId = principal.hospitalId

        val requestFlow: Flow<List<EmergencyRequestEntity>> = when {
            principal.role == AppRole.SYSTEM_ADMIN -> {
                resQRepository.getAllActiveRequestsStream()
            }
            hospitalId != null && (principal.role == AppRole.HOSPITAL_ADMIN || principal.role == AppRole.DRIVER) -> {
                resQRepository.getRequestsByHospitalStream(hospitalId)
            }
            principal.role == AppRole.PATIENT -> {
                val userId = principal.userId
                if (userId.isNullOrBlank()) {
                    flowOf(emptyList())
                } else {
                    resQRepository.getRequestsByUserStream(userId)
                }
            }
            else -> flowOf(emptyList())
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
        val hospitalId = principal.hospitalId

        val ambulanceFlow: Flow<List<com.example.mobiledev.data.local.entity.AmbulanceEntity>> = when {
            principal.role == AppRole.SYSTEM_ADMIN -> {
                resQRepository.getAllAmbulancesStream()
            }
            hospitalId != null && (principal.role == AppRole.HOSPITAL_ADMIN || principal.role == AppRole.DRIVER) -> {
                resQRepository.getAmbulancesByHospitalStream(hospitalId)
            }
            principal.role == AppRole.PATIENT -> {
                resQRepository.getAllAvailableAmbulancesStream()
            }
            else -> flowOf(emptyList())
        }

        return ambulanceFlow.map { ambulances ->
            ambulances.map { entity ->
                Ambulance(
                    id = entity.id,
                    plateNumber = entity.registrationNo,
                    drivers = entity.driverId, // In a real app we'd look up multiple drivers
                    status = mapAmbulanceStatus(entity.status),
                    currentEmergencyId = null
                )
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

                // Notify patient about status change
                val notification = NotificationEntity(
                    id = "NOTIF_${UUID.randomUUID()}",
                    userId = request.userId,
                    title = "Emergency Request Updated",
                    message = "Your emergency request status is now: ${status.name}",
                    timestamp = System.currentTimeMillis(),
                    type = "STATUS_CHANGE"
                )
                resQRepository.insertNotification(notification)

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

                // Notify driver
                val driverNotification = NotificationEntity(
                    id = "NOTIF_${UUID.randomUUID()}",
                    userId = ambulance.driverId,
                    title = "New Assignment",
                    message = "You have been assigned to a new emergency request.",
                    timestamp = System.currentTimeMillis(),
                    type = "AMBULANCE_ASSIGNED"
                )
                resQRepository.insertNotification(driverNotification)

                // Notify patient
                val patientNotification = NotificationEntity(
                    id = "NOTIF_${UUID.randomUUID()}",
                    userId = request.userId,
                    title = "Ambulance Assigned",
                    message = "An ambulance has been assigned to your request and is on the way.",
                    timestamp = System.currentTimeMillis(),
                    type = "AMBULANCE_ASSIGNED"
                )
                resQRepository.insertNotification(patientNotification)

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
                patientName = entity.userId,
                location = entity.location,
                phoneNumber = "",
                description = entity.description,
                status = mapStatus(entity.status),
                priority = mapPriority(entity.priority),
                timestamp = entity.createdAt,
                assignedAmbulanceId = entity.ambulanceId
            )
        }
    }

    private fun mapPriority(priority: String): EmergencyPriority {
        return when (priority.uppercase()) {
            "CRITICAL" -> EmergencyPriority.CRITICAL
            "HIGH" -> EmergencyPriority.HIGH
            "MEDIUM" -> EmergencyPriority.MEDIUM
            "LOW" -> EmergencyPriority.LOW
            else -> EmergencyPriority.MEDIUM
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