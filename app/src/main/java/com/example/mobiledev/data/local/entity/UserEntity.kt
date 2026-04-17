package com.example.mobiledev.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "USER",
    foreignKeys = [
        ForeignKey(
            entity = HospitalEntity::class,
            parentColumns = ["id"],
            childColumns = ["hospital_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["uuid"], unique = true),
        Index(value = ["hospital_id"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "hospital_id") val hospitalId: String?,
    val name: String,
    val email: String,
    val phone: String,
    val location: String,
    @ColumnInfo(name = "user_type") val userType: String, // PATIENT, HOSPITAL_ADMIN, DRIVER
    val uuid: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
