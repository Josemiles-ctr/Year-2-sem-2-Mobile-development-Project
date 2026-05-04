package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface ResQRepository {
    // User
    fun getAllUsersStream(): Flow<List<UserEntity>>
    suspend fun getUserById(id: String): UserEntity?
    suspend fun getUserByEmail(email: String): UserEntity?
    suspend fun insertUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    suspend fun deleteUser(user: UserEntity)

    // Hospital
    fun getAllHospitalsStream(): Flow<List<HospitalEntity>>
    fun getApprovedHospitalsStream(): Flow<List<HospitalEntity>>
    suspend fun getHospitalById(id: String): HospitalEntity?
    suspend fun getHospitalByAdminId(adminId: String): HospitalEntity?
    suspend fun insertHospital(hospital: HospitalEntity)
    suspend fun updateHospital(hospital: HospitalEntity)
    suspend fun deleteHospital(hospital: HospitalEntity)
    suspend fun loginHospital(email: String, password: String): HospitalEntity?

    // Ambulance
    fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>>
    fun getAllAvailableAmbulancesStream(): Flow<List<AmbulanceEntity>>
    fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>>
    fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>>
    suspend fun getAmbulanceById(id: String): AmbulanceEntity?
    suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity?
    suspend fun insertAmbulance(ambulance: AmbulanceEntity)
    suspend fun updateAmbulance(ambulance: AmbulanceEntity)
    suspend fun deleteAmbulance(ambulance: AmbulanceEntity)

    // Emergency Request
    fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>>
    fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>>
    fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>>
    fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>>
    fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>>
    suspend fun getRequestById(id: String): EmergencyRequestEntity?
    suspend fun insertRequest(request: EmergencyRequestEntity)
    suspend fun updateRequest(request: EmergencyRequestEntity)
    suspend fun deleteRequest(request: EmergencyRequestEntity)
    suspend fun softDeleteRequest(id: String)

    // Notifications
    fun getNotificationsForUserStream(userId: String): Flow<List<com.example.mobiledev.data.local.entity.NotificationEntity>>
    fun getUnreadNotificationCountStream(userId: String): Flow<Int>
    suspend fun insertNotification(notification: com.example.mobiledev.data.local.entity.NotificationEntity)
    suspend fun markNotificationAsRead(id: String)
}
