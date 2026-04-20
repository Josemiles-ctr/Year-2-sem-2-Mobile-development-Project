package com.example.mobiledev.data.local.dao

import androidx.room.*
import com.example.mobiledev.data.local.entity.HospitalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HospitalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospital(hospital: HospitalEntity)

    @Update
    suspend fun updateHospital(hospital: HospitalEntity)

    @Delete
    suspend fun deleteHospital(hospital: HospitalEntity)

    @Query("SELECT * FROM HOSPITAL WHERE id = :id")
    suspend fun getHospitalById(id: String): HospitalEntity?

    @Query("SELECT * FROM HOSPITAL")
    fun getAllHospitals(): Flow<List<HospitalEntity>>

    @Query("SELECT * FROM HOSPITAL WHERE admin_id = :adminId")
    suspend fun getHospitalByAdminId(adminId: String): HospitalEntity?

    @Query("SELECT * FROM HOSPITAL WHERE email = :email")
    suspend fun getHospitalByEmail(email: String): HospitalEntity?
}
