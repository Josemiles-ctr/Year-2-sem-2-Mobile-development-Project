package com.example.mobiledev.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "EMERGENCY_REQUEST",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HospitalEntity::class,
            parentColumns = ["id"],
            childColumns = ["hospital_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AmbulanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["ambulance_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["hospital_id"]),
        Index(value = ["ambulance_id"])
    ]
)
data class EmergencyRequestEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "hospital_id") val hospitalId: String,
    @ColumnInfo(name = "ambulance_id") val ambulanceId: String?,
    val status: String, // PENDING, ASSIGNED, ON_WAY, ARRIVED, COMPLETED
    val description: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val priority: String, // LOW, MEDIUM, HIGH, CRITICAL
    @ColumnInfo(name = "estimated_time_mins") val estimatedTimeMins: Int?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
