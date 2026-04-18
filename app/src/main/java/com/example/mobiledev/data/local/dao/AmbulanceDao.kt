package com.example.mobiledev.data.local.dao

import androidx.room.*
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AmbulanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmbulance(ambulance: AmbulanceEntity)

    @Update
    suspend fun updateAmbulance(ambulance: AmbulanceEntity)

    @Delete
    suspend fun deleteAmbulance(ambulance: AmbulanceEntity)

    @Query("SELECT * FROM AMBULANCE WHERE id = :id")
    suspend fun getAmbulanceById(id: String): AmbulanceEntity?

    @Query("SELECT * FROM AMBULANCE WHERE hospital_id = :hospitalId")
    fun getAmbulancesByHospital(hospitalId: String): Flow<List<AmbulanceEntity>>

    @Query("SELECT * FROM AMBULANCE WHERE driver_id = :driverId")
    suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity?

    @Query("SELECT * FROM AMBULANCE WHERE hospital_id = :hospitalId AND status = 'AVAILABLE'")
    fun getAvailableAmbulances(hospitalId: String): Flow<List<AmbulanceEntity>>

    @Query("SELECT * FROM AMBULANCE")
    fun getAllAmbulances(): Flow<List<AmbulanceEntity>>
}
