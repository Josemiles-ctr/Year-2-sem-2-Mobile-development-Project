package com.example.mobiledev.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "AMBULANCE",
    foreignKeys = [
        ForeignKey(
            entity = HospitalEntity::class,
            parentColumns = ["id"],
            childColumns = ["hospital_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["driver_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["registration_no"], unique = true),
        Index(value = ["license_no"], unique = true),
        Index(value = ["hospital_id"]),
        Index(value = ["driver_id"])
    ]
)
data class AmbulanceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "hospital_id") val hospitalId: String,
    @ColumnInfo(name = "driver_id") val driverId: String,
    @ColumnInfo(name = "registration_no") val registrationNo: String,
    @ColumnInfo(name = "license_no") val licenseNo: String,
    val status: String, // AVAILABLE, ON_EMERGENCY, OFFLINE
    val latitude: Float,
    val longitude: Float,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
