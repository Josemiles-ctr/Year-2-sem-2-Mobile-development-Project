package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.NotificationEntity
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.data.security.Permission
import com.example.mobiledev.data.security.RbacPolicy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

class FirebaseResQRepository(
    private val db: FirebaseDatabase,
    private val authSessionManager: AuthSessionManager
) : ResQRepository {

    private val usersRef: DatabaseReference = db.getReference(USERS_NODE)
    private val hospitalsRef: DatabaseReference = db.getReference(HOSPITALS_NODE)
    private val ambulancesRef: DatabaseReference = db.getReference(AMBULANCES_NODE)
    private val requestsRef: DatabaseReference = db.getReference(REQUESTS_NODE)
    private val notificationsRef: DatabaseReference = db.getReference(NOTIFICATIONS_NODE)

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

    override fun getAllUsersStream(): Flow<List<UserEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return usersRef.observeList { it.toUserEntityOrNull() }
    }

    override suspend fun getUserById(id: String): UserEntity? {
        val principal = principal()
        if (principal.role != AppRole.SYSTEM_ADMIN) {
            RbacPolicy.requireUserScope(principal, id)
        }
        return usersRef.child(id).get().await().toUserEntityOrNull()
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        requirePermission(Permission.MANAGE_USERS)
        val normalized = email.trim().lowercase()
        return usersRef.get().await().children
            .mapNotNull { it.toUserEntityOrNull() }
            .firstOrNull { it.email.trim().lowercase() == normalized }
    }

    override suspend fun insertUser(user: UserEntity) {
        requirePermission(Permission.MANAGE_USERS)
        usersRef.child(user.id).setValue(user.toPayload()).await()
    }

    override suspend fun updateUser(user: UserEntity) {
        val principal = principal()
        if (principal.role != AppRole.SYSTEM_ADMIN) {
            RbacPolicy.requireUserScope(principal, user.id)
        }
        usersRef.child(user.id).updateChildren(user.toPayload()).await()
    }

    override suspend fun deleteUser(user: UserEntity) {
        requirePermission(Permission.MANAGE_USERS)
        usersRef.child(user.id).removeValue().await()
    }

    override fun getAllHospitalsStream(): Flow<List<HospitalEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return hospitalsRef.observeList { it.toHospitalEntityOrNull() }
    }

    override fun getApprovedHospitalsStream(): Flow<List<HospitalEntity>> {
        requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
        return hospitalsRef.observeList { it.toHospitalEntityOrNull() }.map { list ->
            list.filter { it.status == HospitalStatus.APPROVED }
        }
    }

    override suspend fun getHospitalById(id: String): HospitalEntity? {
        val hospital = hospitalsRef.child(id).get().await().toHospitalEntityOrNull() ?: return null
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
        return hospitalsRef.get().await().children
            .mapNotNull { it.toHospitalEntityOrNull() }
            .firstOrNull { it.adminId == adminId }
    }

    override suspend fun insertHospital(hospital: HospitalEntity) {
        requirePermission(Permission.MANAGE_USERS)
        hospitalsRef.child(hospital.id).setValue(hospital.toPayload()).await()
    }

    override suspend fun updateHospital(hospital: HospitalEntity) {
        requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, hospital.id)
        hospitalsRef.child(hospital.id).updateChildren(hospital.toPayload()).await()
    }

    override suspend fun deleteHospital(hospital: HospitalEntity) {
        requirePermission(Permission.MANAGE_USERS)
        hospitalsRef.child(hospital.id).removeValue().await()
    }

    override suspend fun loginHospital(email: String, password: String): HospitalEntity? {
        val normalized = email.trim().lowercase()
        val hospital = hospitalsRef.get().await().children
            .mapNotNull { it.toHospitalEntityOrNull() }
            .firstOrNull { it.email.trim().lowercase() == normalized }
            ?: return null

        return if (BCrypt.checkpw(password, hospital.passwordHash)) hospital else null
    }

    override fun getAllAmbulancesStream(): Flow<List<AmbulanceEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return ambulancesRef.observeList { it.toAmbulanceEntityOrNull() }
    }

    override fun getAllAvailableAmbulancesStream(): Flow<List<AmbulanceEntity>> {
        requirePermission(Permission.VIEW_AMBULANCES)
        return ambulancesRef.observeList { it.toAmbulanceEntityOrNull() }.map { list ->
            list.filter { it.status == "AVAILABLE" }
        }
    }

    override fun getAmbulancesByHospitalStream(hospitalId: String): Flow<List<AmbulanceEntity>> {
        return flow {
            val principal = principal()
            when (principal.role) {
                AppRole.PATIENT -> {
                    requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
                    val hospital = getHospitalById(hospitalId) ?: throw SecurityException("Hospital not found")
                    if (hospital.status != HospitalStatus.APPROVED) {
                        throw SecurityException("Access denied: only approved hospitals are visible to patients")
                    }
                }
                else -> requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, hospitalId)
            }

            emitAll(ambulancesRef.observeList { it.toAmbulanceEntityOrNull() }.map { list ->
                list.filter { it.hospitalId == hospitalId }
            })
        }
    }

    override fun getAvailableAmbulancesStream(hospitalId: String): Flow<List<AmbulanceEntity>> {
        requireHospitalScope(Permission.MANAGE_REQUESTS, hospitalId)
        return ambulancesRef.observeList { it.toAmbulanceEntityOrNull() }.map { list ->
            list.filter { it.hospitalId == hospitalId && it.status.equals(AVAILABLE_STATUS, ignoreCase = true) }
        }
    }

    override suspend fun getAmbulanceById(id: String): AmbulanceEntity? {
        val ambulance = ambulancesRef.child(id).get().await().toAmbulanceEntityOrNull() ?: return null
        val principal = principal()
        if (principal.role == AppRole.PATIENT) {
            requirePermission(Permission.VIEW_APPROVED_HOSPITALS)
            val hospital = getHospitalById(ambulance.hospitalId) ?: throw SecurityException("Hospital not found")
            if (hospital.status != HospitalStatus.APPROVED) {
                throw SecurityException("Access denied: only approved hospitals' ambulances are visible to patients")
            }
        } else {
            requireHospitalScope(Permission.VIEW_HOSPITAL_DATA, ambulance.hospitalId)
        }
        return ambulance
    }

    override suspend fun getAmbulanceByDriverId(driverId: String): AmbulanceEntity? {
        val principal = principal()
        if (principal.role == AppRole.DRIVER) {
            RbacPolicy.requireUserScope(principal, driverId)
        } else {
            requirePermission(Permission.MANAGE_REQUESTS)
        }

        return ambulancesRef.get().await().children
            .mapNotNull { it.toAmbulanceEntityOrNull() }
            .firstOrNull { it.driverId == driverId }
    }

    override suspend fun insertAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulancesRef.child(ambulance.id).setValue(ambulance.toPayload()).await()
    }

    override suspend fun updateAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulancesRef.child(ambulance.id).updateChildren(ambulance.toPayload()).await()
    }

    override suspend fun deleteAmbulance(ambulance: AmbulanceEntity) {
        requireHospitalScope(Permission.MANAGE_AMBULANCES, ambulance.hospitalId)
        ambulancesRef.child(ambulance.id).removeValue().await()
    }

    override fun getAllActiveRequestsStream(): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.VIEW_SYSTEM_DATA)
        return requestsRef.observeList { it.toEmergencyRequestEntityOrNull() }.map { list ->
            list.filterNot { it.isDeleted }
        }
    }

    override fun getRequestsByUserStream(userId: String): Flow<List<EmergencyRequestEntity>> {
        val principal = requirePermission(Permission.VIEW_OWN_REQUESTS)
        RbacPolicy.requireUserScope(principal, userId)
        return requestsRef.observeList { it.toEmergencyRequestEntityOrNull() }.map { list ->
            list.filter { it.userId == userId && !it.isDeleted }
        }
    }

    override fun getRequestsByHospitalStream(hospitalId: String): Flow<List<EmergencyRequestEntity>> {
        requireHospitalScope(Permission.MANAGE_REQUESTS, hospitalId)
        return requestsRef.observeList { it.toEmergencyRequestEntityOrNull() }.map { list ->
            list.filter { it.hospitalId == hospitalId && !it.isDeleted }
        }
    }

    override fun getRequestsByAmbulanceStream(ambulanceId: String): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.MANAGE_REQUESTS)
        return requestsRef.observeList { it.toEmergencyRequestEntityOrNull() }.map { list ->
            list.filter { it.ambulanceId == ambulanceId && !it.isDeleted }
        }
    }

    override fun getRequestsByStatusStream(status: String): Flow<List<EmergencyRequestEntity>> {
        requirePermission(Permission.MANAGE_REQUESTS)
        return requestsRef.observeList { it.toEmergencyRequestEntityOrNull() }.map { list ->
            list.filter { it.status.equals(status, ignoreCase = true) && !it.isDeleted }
        }
    }

    override suspend fun getRequestById(id: String): EmergencyRequestEntity? {
        val request = requestsRef.child(id).get().await().toEmergencyRequestEntityOrNull() ?: return null
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
        requestsRef.child(request.id).setValue(request.toPayload()).await()
    }

    override suspend fun updateRequest(request: EmergencyRequestEntity) {
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        requestsRef.child(request.id).updateChildren(request.toPayload()).await()
    }

    override suspend fun deleteRequest(request: EmergencyRequestEntity) {
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        requestsRef.child(request.id).removeValue().await()
    }

    override suspend fun softDeleteRequest(id: String) {
        val request = getRequestById(id) ?: throw IllegalArgumentException("Request not found: $id")
        requireHospitalScope(Permission.MANAGE_REQUESTS, request.hospitalId)
        requestsRef.child(id).child("isDeleted").setValue(true).await()
    }

    // Notifications
    override fun getNotificationsForUserStream(userId: String): Flow<List<NotificationEntity>> {
        val principal = principal()
        RbacPolicy.requireUserScope(principal, userId)
        return notificationsRef.observeList { it.toNotificationEntityOrNull() }.map { list ->
            list.filter { it.userId == userId }.sortedByDescending { it.timestamp }
        }
    }

    override fun getUnreadNotificationCountStream(userId: String): Flow<Int> {
        val principal = principal()
        RbacPolicy.requireUserScope(principal, userId)
        return getNotificationsForUserStream(userId).map { list ->
            list.count { !it.isRead }
        }
    }

    override suspend fun insertNotification(notification: NotificationEntity) {
        notificationsRef.child(notification.id).setValue(notification.toPayload()).await()
    }

    override suspend fun markNotificationAsRead(id: String) {
        notificationsRef.child(id).child("isRead").setValue(true).await()
    }

    private fun DataSnapshot.toUserEntityOrNull(): UserEntity? {
        val id = child("id").getValue(String::class.java) ?: key.orEmpty()
        if (id.isBlank()) return null
        return UserEntity(
            id = id,
            hospitalId = child("hospitalId").getValue(String::class.java),
            name = child("name").getValue(String::class.java).orEmpty(),
            email = child("email").getValue(String::class.java).orEmpty(),
            phone = child("phone").getValue(String::class.java).orEmpty(),
            location = child("location").getValue(String::class.java).orEmpty(),
            userType = child("userType").getValue(String::class.java)
                ?: child("role").getValue(String::class.java)
                ?: AppRole.PATIENT.name,
            uuid = child("uuid").getValue(String::class.java),
            createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
            updatedAt = child("updatedAt").getValue(Long::class.java) ?: 0L
        )
    }

    private fun UserEntity.toPayload(): Map<String, Any?> = mapOf(
        "id" to id,
        "hospitalId" to hospitalId,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "location" to location,
        "userType" to userType,
        "role" to userType,
        "uuid" to uuid,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    private fun DataSnapshot.toHospitalEntityOrNull(): HospitalEntity? {
        val id = child("id").getValue(String::class.java) ?: key.orEmpty()
        if (id.isBlank()) return null
        val statusRaw = child("status").getValue(String::class.java) ?: HospitalStatus.APPROVED.name
        val status = runCatching { HospitalStatus.valueOf(statusRaw) }.getOrDefault(HospitalStatus.APPROVED)

        return HospitalEntity(
            id = id,
            adminId = child("adminId").getValue(String::class.java).orEmpty(),
            name = child("name").getValue(String::class.java).orEmpty(),
            email = child("email").getValue(String::class.java).orEmpty(),
            phone = child("phone").getValue(String::class.java).orEmpty(),
            location = child("location").getValue(String::class.java).orEmpty(),
            latitude = child("latitude").getValue(Double::class.java),
            longitude = child("longitude").getValue(Double::class.java),
            uuid = child("uuid").getValue(String::class.java),
            passwordHash = child("passwordHash").getValue(String::class.java).orEmpty(),
            status = status,
            activeAmbulances = child("activeAmbulances").getValue(Int::class.java)
                ?: child("activeAmbulances").getValue(Long::class.java)?.toInt()
                ?: 0,
            createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
            updatedAt = child("updatedAt").getValue(Long::class.java) ?: 0L
        )
    }

    private fun HospitalEntity.toPayload(): Map<String, Any?> = mapOf(
        "id" to id,
        "adminId" to adminId,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "location" to location,
        "latitude" to latitude,
        "longitude" to longitude,
        "uuid" to uuid,
        "passwordHash" to passwordHash,
        "status" to status.name,
        "activeAmbulances" to activeAmbulances,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    private fun DataSnapshot.toAmbulanceEntityOrNull(): AmbulanceEntity? {
        val id = child("id").getValue(String::class.java) ?: key.orEmpty()
        if (id.isBlank()) return null

        return AmbulanceEntity(
            id = id,
            hospitalId = child("hospitalId").getValue(String::class.java).orEmpty(),
            driverId = child("driverId").getValue(String::class.java).orEmpty(),
            registrationNo = child("registrationNo").getValue(String::class.java).orEmpty(),
            licenseNo = child("licenseNo").getValue(String::class.java).orEmpty(),
            status = child("status").getValue(String::class.java).orEmpty(),
            latitude = child("latitude").getValue(Double::class.java) ?: 0.0,
            longitude = child("longitude").getValue(Double::class.java) ?: 0.0,
            createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
            updatedAt = child("updatedAt").getValue(Long::class.java) ?: 0L
        )
    }

    private fun AmbulanceEntity.toPayload(): Map<String, Any?> = mapOf(
        "id" to id,
        "hospitalId" to hospitalId,
        "driverId" to driverId,
        "registrationNo" to registrationNo,
        "licenseNo" to licenseNo,
        "status" to status,
        "latitude" to latitude,
        "longitude" to longitude,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    private fun DataSnapshot.toEmergencyRequestEntityOrNull(): EmergencyRequestEntity? {
        val id = child("id").getValue(String::class.java) ?: key.orEmpty()
        if (id.isBlank()) return null

        return EmergencyRequestEntity(
            id = id,
            userId = child("userId").getValue(String::class.java).orEmpty(),
            hospitalId = child("hospitalId").getValue(String::class.java).orEmpty(),
            ambulanceId = child("ambulanceId").getValue(String::class.java),
            status = child("status").getValue(String::class.java).orEmpty(),
            description = child("description").getValue(String::class.java).orEmpty(),
            location = child("location").getValue(String::class.java).orEmpty(),
            latitude = child("latitude").getValue(Double::class.java),
            longitude = child("longitude").getValue(Double::class.java),
            priority = child("priority").getValue(String::class.java).orEmpty(),
            estimatedTimeMins = child("estimatedTimeMins").getValue(Int::class.java)
                ?: child("estimatedTimeMins").getValue(Long::class.java)?.toInt(),
            createdAt = child("createdAt").getValue(Long::class.java) ?: 0L,
            updatedAt = child("updatedAt").getValue(Long::class.java) ?: 0L,
            completedAt = child("completedAt").getValue(Long::class.java),
            isDeleted = child("isDeleted").getValue(Boolean::class.java) ?: false
        )
    }

    private fun EmergencyRequestEntity.toPayload(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "hospitalId" to hospitalId,
        "ambulanceId" to ambulanceId,
        "status" to status,
        "description" to description,
        "location" to location,
        "latitude" to latitude,
        "longitude" to longitude,
        "priority" to priority,
        "estimatedTimeMins" to estimatedTimeMins,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "completedAt" to completedAt,
        "isDeleted" to isDeleted
    )

    private fun DataSnapshot.toNotificationEntityOrNull(): NotificationEntity? {
        val id = child("id").getValue(String::class.java) ?: key.orEmpty()
        if (id.isBlank()) return null
        return NotificationEntity(
            id = id,
            userId = child("userId").getValue(String::class.java).orEmpty(),
            title = child("title").getValue(String::class.java).orEmpty(),
            message = child("message").getValue(String::class.java).orEmpty(),
            timestamp = child("timestamp").getValue(Long::class.java) ?: 0L,
            isRead = child("isRead").getValue(Boolean::class.java) ?: false,
            type = child("type").getValue(String::class.java).orEmpty()
        )
    }

    private fun NotificationEntity.toPayload(): Map<String, Any?> = mapOf(
        "id" to id,
        "userId" to userId,
        "title" to title,
        "message" to message,
        "timestamp" to timestamp,
        "isRead" to isRead,
        "type" to type
    )

    private fun <T> DatabaseReference.observeList(parser: (DataSnapshot) -> T?): Flow<List<T>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull(parser))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }

        addValueEventListener(listener)
        awaitClose { removeEventListener(listener) }
    }

    private companion object {
        const val USERS_NODE = "users"
        const val HOSPITALS_NODE = "hospitals"
        const val AMBULANCES_NODE = "ambulances"
        const val REQUESTS_NODE = "emergencyRequests"
        const val NOTIFICATIONS_NODE = "notifications"
        const val AVAILABLE_STATUS = "AVAILABLE"
    }
}


