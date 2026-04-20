package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.*
import com.example.mobiledev.data.services.StaffApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface StaffRepository {
    fun getStaffList(): Flow<List<StaffMember>>
    fun getPendingInvitations(): Flow<List<StaffInvitation>>
    suspend fun inviteStaff(email: String, role: StaffRole): Result<Unit>
    suspend fun updateStaff(id: String, role: StaffRole?, status: StaffStatus?): Result<Unit>
    suspend fun removeStaff(id: String): Result<Unit>
    suspend fun resendInvitation(id: String): Result<Unit>
    suspend fun cancelInvitation(id: String): Result<Unit>
}

class ApiStaffRepository(private val apiService: StaffApiService) : StaffRepository {
    override fun getStaffList(): Flow<List<StaffMember>> = flow {
        emit(apiService.getStaffList())
    }

    override fun getPendingInvitations(): Flow<List<StaffInvitation>> = flow {
        emit(apiService.getPendingInvitations())
    }

    override suspend fun inviteStaff(email: String, role: StaffRole): Result<Unit> {
        return try {
            val response = apiService.inviteStaff(InviteStaffRequest(email, role))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Invite failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStaff(id: String, role: StaffRole?, status: StaffStatus?): Result<Unit> {
        return try {
            val response = apiService.updateStaff(id, UpdateStaffRequest(role, status))
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Update failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeStaff(id: String): Result<Unit> {
        return try {
            val response = apiService.removeStaff(id)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Remove failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendInvitation(id: String): Result<Unit> {
        return try {
            val response = apiService.resendInvitation(id)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Resend failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelInvitation(id: String): Result<Unit> {
        return try {
            val response = apiService.cancelInvitation(id)
            if (response.isSuccessful) Result.success(Unit) else Result.failure(Exception("Cancel failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
