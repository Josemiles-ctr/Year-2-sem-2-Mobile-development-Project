package com.example.mobiledev.data.local.dao

import androidx.room.*
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: EmergencyRequestEntity)

    @Update
    suspend fun updateRequest(request: EmergencyRequestEntity)

    @Delete
    suspend fun deleteRequest(request: EmergencyRequestEntity)

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE id = :id AND is_deleted = 0")
    suspend fun getRequestById(id: String): EmergencyRequestEntity?

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE user_id = :userId AND is_deleted = 0")
    fun getRequestsByUser(userId: String): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE hospital_id = :hospitalId AND is_deleted = 0")
    fun getRequestsByHospital(hospitalId: String): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE ambulance_id = :ambulanceId AND is_deleted = 0")
    fun getRequestsByAmbulance(ambulanceId: String): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE status = :status AND is_deleted = 0")
    fun getRequestsByStatus(status: String): Flow<List<EmergencyRequestEntity>>

    @Query("SELECT * FROM EMERGENCY_REQUEST WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getAllActiveRequests(): Flow<List<EmergencyRequestEntity>>

    @Query("UPDATE EMERGENCY_REQUEST SET is_deleted = 1 WHERE id = :id")
    suspend fun softDeleteRequest(id: String)
}
