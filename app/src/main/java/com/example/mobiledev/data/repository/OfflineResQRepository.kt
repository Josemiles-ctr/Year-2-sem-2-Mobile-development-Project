package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.dao.AmbulanceDao
import com.example.mobiledev.data.local.dao.EmergencyRequestDao
import com.example.mobiledev.data.local.dao.HospitalDao
import com.example.mobiledev.data.local.dao.UserDao
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.data.security.Permission
import com.example.mobiledev.data.security.RbacPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.mindrot.jbcrypt.BCrypt

class OfflineResQRepository(
    private val userDao: UserDao,
    private val hospitalDao: HospitalDao,
    private val ambulanceDao: AmbulanceDao,
    private val emergencyRequestDao: EmergencyRequestDao,
    private val authSessionManager: AuthSessionManager
) : ResQRepository {
    private fun principal(): AuthPrincipal = authSessionManager.currentPrincipal

    private fun requirePermission(permission: Permission): AuthPrincipal {
        val principal = principal()
        RbacPolicy.requirePermission(principal, permission)
        return principal
    }

    private fun requireHospitalScope(permission: Permission, hospitalId: String): AuthPrincipal {
        val principal = requirePermission(permission)
        RbacPolicy.requireHospitalScope(principal, hospitalId)
        return principal
    }

    // User
    override fun getAllUsersStream(): Flow<List<UserEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return userDao.getAllUsers()
    }

    override suspend fun getUserById(id: String): UserEntity? {
        val principal = principal()
        if (principal.role != AppRole.SYSTEM_ADMIN) {
            RbacPolicy.requireUserScope(principal, id)
        }
        return userDao.getUserById(id)
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        requirePermission(Permission.MANAGE_USERS)
        return userDao.getUserByEmail(email)
    }

    override suspend fun insertUser(user: UserEntity) {
        requirePermission(Permission.MANAGE_USERS)
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        val principal = principal()
        if (principal.role != AppRole.SYSTEM_ADMIN) {
            RbacPolicy.requireUserScope(principal, user.id)
        }
        userDao.updateUser(user)
    }

    override suspend fun deleteUser(user: UserEntity) {
        requirePermission(Permission.MANAGE_USERS)
        userDao.deleteUser(user)
    }

    // Hospital
    override fun getAllHospitalsStream(): Flow<List<HospitalEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return hospitalDao.getAllHospitals()
    }

    override fun getApprovedHospitalsStream(): Flow<List<HospitalEntity>> {
        requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
        return hospitalDao.getApprovedHospitals()
    }

    override suspend fun getHospitalById(id: String): HospitalEntity? {
        val hospital = hospitalDao.getHospitalById(id) ?: return null
        val principal = principal()

        return when (principal.role) {
            AppRole.SYSTEM_ADMIN -> hospital
            AppRole.PATIENT -> {
                requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
                if (hospital.status != HospitalStatus.APPROVED) {
                    throw SecurityException("Access denied: only approved hospitals are visible to patients")
                }
                hospital
            }
            else -> {
                RbacPolicy.requireHospitalScope(principal, id)
                hospital
            }
        }
    }

    override suspend fun getHospitalByAdminId(adminId: String): HospitalEntity? {
        val principal = principal()
        if (principal.role != AppRole.SYSTEM_ADMIN) {
            RbacPolicy.requireUserScope(principal, adminId)
        }
        return hospitalDao.getHospitalByAdminId(adminId)
    }

    override suspend fun insertHospital(hospital: HospitalEntity) {
        requirePermission(Permission.MANAGE_USERS)
        hospitalDao.insertHospital(hospital)
    }

    override suspend fun updateHospital(hospital: HospitalEntity) {
        requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, hospital.id)
        hospitalDao.updateHospital(hospital)
    }

    override suspend fun deleteHospital(hospital: HospitalEntity) {
        requirePermission(Permission.MANAGE_USERS)
        hospitalDao.deleteHospital(hospital)
    }

    override suspend fun loginHospital(email: String, password: String): HospitalEntity? {
        val hospital = hospitalDao.getHospitalByEmail(email)
        return if (hospital != null && BCrypt.checkpw(password, hospital.passwordHash)) {
            hospital
        } else {
            null
        }
    }

    // Ambulance
    override fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return ambulanceDao.getAllAmbulances()
    }

    override fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>> {
        return flow {
            val principal = principal()
            when (principal.role) {
                AppRole.PATIENT -> {
                    requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
                    val hospital = hospitalDao.getHospitalById(hospitalId)
                        ?: throw SecurityException("Hospital not found")
                    if (hospital.status != HospitalStatus.APPROVED) {
                        throw SecurityException("Access denied: only approved hospitals are visible to patients")
                    }
                }
                else -> requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, hospitalId)
            }
            emitAll(ambulanceDao.getAmbulancesByHospital(hospitalId))
        }
    }

    override fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>> {
        requireHospitalScope(Permission.MANAGE_REQUESTS, hospitalId)
        return ambulanceDao.getAvailableAmbulances(hospitalId)
    }

    override suspend fun getAmbulanceById(id: String): AmbulanceEntity? {
        val ambulance = ambulanceDao.getAmbulanceById(id) ?: return null
        requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, ambulance.hospitalId)
        return ambulance
    }

    override suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity? {
        val principal = principal()
        if (principal.role == AppRole.DRIVER) {
            RbacPolicy.requireUserScope(principal, driverId)
        } else {
            requirePermission(Permission.MANAGE_REQUESTS)
        }
        return ambulanceDao.getAmbulanceByDriverId(driverId)
    }

    override suspend fun insertAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulanceDao.insertAmbulance(ambulance)
    }

    override suspend fun updateAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulanceDao.updateAmbulance(ambulance)
    }

    override suspend fun deleteAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulanceDao.deleteAmbulance(ambulance)
    }

    // Emergency Request
    override fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return emergencyRequestDao.getAllActiveRequests()
    }

    override fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>> {
        val principal = requirePermission(Permission.VIEW_OWN_REQUESTS)
        RbacPolicy.requireUserScope(principal, userId)
        return emergencyRequestDao.getRequestsByUser(userId)
    }

    override fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>> {
        requireHospitalScope(Permission.MANAGE_REQUESTS, hospitalId)
        return emergencyRequestDao.getRequestsByHospital(hospitalId)
    }

    override fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.MANAGE_REQUESTS)
        return emergencyRequestDao.getRequestsByAmbulance(ambulanceId)
    }

    override fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.MANAGE_REQUESTS)
        return emergencyRequestDao.getRequestsByStatus(status)
    }

    override suspend fun getRequestById(id: String): EmergencyRequestEntity? {
        val request = emergencyRequestDao.getRequestById(id) ?: return null
        val principal = principal()

        when (principal.role) {
            AppRole.SYSTEM_ADMIN -> Unit
            AppRole.HOSPITAL_ADMIN -> {
                RbacPolicy.requirePermission(principal, Permission.MANAGE_REQUESTS)
                RbacPolicy.requireHospitalScope(principal, request.hospitalId)
            }
            AppRole.PATIENT -> {
                RbacPolicy.requirePermission(principal, Permission.VIEW_OWN_REQUESTS)
                RbacPolicy.requireUserScope(principal, request.userId)
            }
            else -> throw SecurityException("Access denied: request visibility is restricted")
        }

        return request
    }

    override suspend fun insertRequest(request: EmergencyRequestEntity) {
        val principal = principal()
        when (principal.role) {
            AppRole.SYSTEM_ADMIN -> Unit
            AppRole.HOSPITAL_ADMIN -> {
                RbacPolicy.requirePermission(principal, Permission.CREATE_REQUEST)
                RbacPolicy.requireHospitalScope(principal, request.hospitalId)
            }
            AppRole.PATIENT -> {
                RbacPolicy.requirePermission(principal, Permission.CREATE_REQUEST)
                RbacPolicy.requireUserScope(principal, request.userId)
            }
            else -> throw SecurityException("Access denied: cannot create emergency requests")
        }
        emergencyRequestDao.insertRequest(request)
    }

    override suspend fun updateRequest(request: EmergencyRequestEntity) {
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        emergencyRequestDao.updateRequest(request)
    }

    override suspend fun deleteRequest(request: EmergencyRequestEntity) {
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        emergencyRequestDao.deleteRequest(request)
    }

    override suspend fun softDeleteRequest(id: String) {
        val request = emergencyRequestDao.getRequestById(id)
            ?: throw IllegalArgumentException("Request not found: $id")
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        emergencyRequestDao.softDeleteRequest(id)
    }
}
