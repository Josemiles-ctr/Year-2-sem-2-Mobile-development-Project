package com.example.mobiledev.data.model

enum class EmergencyStatus {
    PENDING,
    ASSIGNED,
    EN_ROUTE,
    ARRIVED,
    COMPLETED,
    CANCELLED
}

enum class EmergencyPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}

data class EmergencyRequest(
    val id: String,
    val patientName: String,
    val location: String,
    val phoneNumber: String,
    val description: String,
    val status: EmergencyStatus,
    val priority: EmergencyPriority,
    val timestamp: Long,
    val assignedAmbulanceId: String? = null
)

enum class AmbulanceStatus {
    AVAILABLE,
    ON_MISSION,
    MAINTENANCE
}

data class Ambulance(
    val id: String,
    val plateNumber: String,
    val driverName: String,
    val status: AmbulanceStatus,
    val currentEmergencyId: String? = null
)
