package com.example.mobiledev.data.model

enum class StaffRole {
    REQUEST_REVIEWER,
    AMBULANCE_COORDINATOR,
    FULL_ADMIN
}

enum class StaffStatus {
    ACTIVE,
    INACTIVE
}

data class StaffMember(
    val id: String,
    val name: String,
    val email: String,
    val role: StaffRole,
    val status: StaffStatus
)

data class StaffInvitation(
    val id: String,
    val email: String,
    val role: StaffRole,
    val createdAt: Long,
    val expiresAt: Long,
    val status: String = "PENDING"
)

data class InviteStaffRequest(
    val email: String,
    val role: StaffRole
)

data class UpdateStaffRequest(
    val role: StaffRole? = null,
    val status: StaffStatus? = null
)
