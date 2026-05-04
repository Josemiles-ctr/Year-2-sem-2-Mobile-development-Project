package com.example.mobiledev.data.model

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType
)

enum class NotificationType {
    EMERGENCY_REQUEST,
    STATUS_CHANGE,
    AMBULANCE_ASSIGNED,
    SYSTEM_ALERT
}
