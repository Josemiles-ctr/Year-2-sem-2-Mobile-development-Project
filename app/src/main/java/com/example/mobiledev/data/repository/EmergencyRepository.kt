package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface EmergencyRepository {
    fun getEmergencyRequests(
        status: String? = null,
        dateFrom: Long? = null,
        dateTo: Long? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<EmergencyRequest>>
    
    fun getAmbulances(): Flow<List<Ambulance>>
    suspend fun updateEmergencyStatus(id: String, status: EmergencyStatus): Result<Unit>
    suspend fun assignAmbulance(requestId: String, ambulanceId: String): Result<Unit>
}

class ApiEmergencyRepository(private val apiService: EmergencyApiService) : EmergencyRepository {
    override fun getEmergencyRequests(
        status: String?,
        dateFrom: Long?,
        dateTo: Long?,
        limit: Int,
        offset: Int
    ): Flow<List<EmergencyRequest>> = flow {
        emit(apiService.getEmergencyRequests(status, dateFrom, dateTo, limit, offset))
    }

    override fun getAmbulances(): Flow<List<Ambulance>> = flow {
        emit(apiService.getAmbulances())
    }

    override suspend fun updateEmergencyStatus(id: String, status: EmergencyStatus): Result<Unit> {
        return try {
            val response = apiService.updateEmergencyStatus(id, status)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Update failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignAmbulance(requestId: String, ambulanceId: String): Result<Unit> {
        return try {
            val response = apiService.assignAmbulance(requestId, ambulanceId)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Assignment failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
