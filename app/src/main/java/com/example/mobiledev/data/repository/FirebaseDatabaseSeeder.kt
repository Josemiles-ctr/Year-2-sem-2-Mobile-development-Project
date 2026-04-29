package com.example.mobiledev.data.repository

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.mock.ActivityMetricType
import com.example.mobiledev.data.mock.MockActivityData
import com.example.mobiledev.data.mock.MockSeedData
import com.example.mobiledev.data.security.AppRole
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

class FirebaseDatabaseSeeder(private val db: FirebaseDatabase) {

    private val usersRef = db.getReference("users")
    private val hospitalsRef = db.getReference("hospitals")
    private val ambulancesRef = db.getReference("ambulances")
    private val requestsRef = db.getReference("emergencyRequests")
    private val activityStatsRef = db.getReference("activity_stats")

    suspend fun seedIfNeeded() {
        val usersSnapshot = usersRef.get().await()
        if (usersSnapshot.exists()) return

        val now = System.currentTimeMillis()
        val defaultPasswordHash = BCrypt.hashpw("password123", BCrypt.gensalt())
        
        val seedBundle = MockSeedData.create(now, defaultPasswordHash)

        // Seed Users
        seedBundle.users.forEach { user ->
            usersRef.child(user.id).setValue(user.toPayload(defaultPasswordHash)).await()
        }

        // Add System Admin
        val sysAdmin = UserEntity(
            id = "SYS_ADMIN",
            hospitalId = null,
            name = "System Administrator",
            email = "admin@resq.local",
            phone = "555-0000",
            location = "Main HQ",
            userType = AppRole.SYSTEM_ADMIN.name,
            uuid = "UUID_SYS_ADMIN",
            createdAt = now,
            updatedAt = now
        )
        usersRef.child(sysAdmin.id).setValue(sysAdmin.toPayload(defaultPasswordHash)).await()

        // Seed Hospitals
        seedBundle.hospitals.forEach { hospital ->
            hospitalsRef.child(hospital.id).setValue(hospital.toPayload()).await()
        }

        // Seed Ambulances
        seedBundle.ambulances.forEach { ambulance ->
            ambulancesRef.child(ambulance.id).setValue(ambulance.toPayload()).await()
        }

        // Seed Requests
        seedBundle.requests.forEach { request ->
            requestsRef.child(request.id).setValue(request.toPayload()).await()
        }

        // Seed Activity Stats
        MockActivityData.miniStats.forEachIndexed { index, stat ->
            activityStatsRef.child("mini_stats").child(index.toString()).setValue(
                mapOf("label" to stat.label, "value" to stat.value)
            ).await()
        }

        MockActivityData.summaries.forEachIndexed { index, summary ->
            activityStatsRef.child("summaries").child(index.toString()).setValue(
                mapOf(
                    "title" to summary.title,
                    "description" to summary.description,
                    "value" to summary.value,
                    "period" to summary.period,
                    "type" to summary.type.name
                )
            ).await()
        }
    }

    private fun UserEntity.toPayload(passwordHash: String): Map<String, Any?> = mapOf(
        "id" to id,
        "hospitalId" to hospitalId,
        "name" to name,
        "email" to email,
        "phone" to phone,
        "location" to location,
        "userType" to userType,
        "role" to userType,
        "uuid" to uuid,
        "password" to passwordHash,
        "accountStatus" to "ACTIVE",
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "emailKey" to email.lowercase(),
        "phoneKey" to phone.filter { it.isDigit() }
    )

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
}
