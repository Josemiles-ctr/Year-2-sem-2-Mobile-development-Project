package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.dao.AmbulanceDao
import com.example.mobiledev.data.local.dao.EmergencyRequestDao
import com.example.mobiledev.data.local.dao.HospitalDao
import com.example.mobiledev.data.local.dao.UserDao
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import org.mindrot.jbcrypt.BCrypt

class OfflineResQRepository(
    private val userDao: UserDao,
    private val hospitalDao: HospitalDao,
    private val ambulanceDao: AmbulanceDao,
    private val emergencyRequestDao: EmergencyRequestDao
) : ResQRepository {
    // User
    override fun getAllUsersStream(): Flow<List<UserEntity>> = userDao.getAllUsers()
    override suspend fun getUserById(id: String): UserEntity? = userDao.getUserById(id)
    override suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    override suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    override suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    // Hospital
    override fun getAllHospitalsStream(): Flow<List<HospitalEntity>> = hospitalDao.getAllHospitals()
    override suspend fun getHospitalById(id: String): HospitalEntity? = hospitalDao.getHospitalById(id)
    override suspend fun getHospitalByAdminId(adminId: String): HospitalEntity? = hospitalDao.getHospitalByAdminId(adminId)
    override suspend fun insertHospital(hospital: HospitalEntity) = hospitalDao.insertHospital(hospital)
    override suspend fun updateHospital(hospital: HospitalEntity) = hospitalDao.updateHospital(hospital)
    override suspend fun deleteHospital(hospital: HospitalEntity) = hospitalDao.deleteHospital(hospital)

    override suspend fun loginHospital(email: String, password: String): HospitalEntity? {
        val hospital = hospitalDao.getHospitalByEmail(email)
        return if (hospital != null && BCrypt.checkpw(password, hospital.passwordHash)) {
            hospital
        } else {
            null
        }
    }

    // Ambulance
    override fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>> = ambulanceDao.getAllAmbulances()
    override fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>> = 
        ambulanceDao.getAmbulancesByHospital(hospitalId)
    override fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>> = 
        ambulanceDao.getAvailableAmbulances(hospitalId)
    override suspend fun getAmbulanceById(id: String): AmbulanceEntity? = ambulanceDao.getAmbulanceById(id)
    override suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity? = ambulanceDao.getAmbulanceByDriverId(driverId)
    override suspend fun insertAmbulance(ambulance: AmbulanceEntity) = ambulanceDao.insertAmbulance(ambulance)
    override suspend fun updateAmbulance(ambulance: AmbulanceEntity) = ambulanceDao.updateAmbulance(ambulance)
    override suspend fun deleteAmbulance(ambulance: AmbulanceEntity) = ambulanceDao.deleteAmbulance(ambulance)

    // Emergency Request
    override fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>> = 
        emergencyRequestDao.getAllActiveRequests()
    override fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>> = 
        emergencyRequestDao.getRequestsByUser(userId)
    override fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>> = 
        emergencyRequestDao.getRequestsByHospital(hospitalId)
    override fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>> = 
        emergencyRequestDao.getRequestsByAmbulance(ambulanceId)
    override fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>> = 
        emergencyRequestDao.getRequestsByStatus(status)
    override suspend fun getRequestById(id: String): EmergencyRequestEntity? = 
        emergencyRequestDao.getRequestById(id)
    override suspend fun insertRequest(request: EmergencyRequestEntity) = 
        emergencyRequestDao.insertRequest(request)
    override suspend fun updateRequest(request: EmergencyRequestEntity) = 
        emergencyRequestDao.updateRequest(request)
    override suspend fun deleteRequest(request: EmergencyRequestEntity) = 
        emergencyRequestDao.deleteRequest(request)
    override suspend fun softDeleteRequest(id: String) = 
        emergencyRequestDao.softDeleteRequest(id)
}
