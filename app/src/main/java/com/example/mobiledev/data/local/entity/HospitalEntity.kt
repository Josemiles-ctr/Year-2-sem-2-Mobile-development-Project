package com.example.mobiledev.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "HOSPITAL",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["admin_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["uuid"], unique = true),
        Index(value = ["admin_id"])
    ]
)
data class HospitalEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "admin_id") val adminId: String,
    val name: String,
    val email: String,
    val phone: String,
    val location: String,
    val latitude: Float?,
    val longitude: Float?,
    val uuid: String?,
    @ColumnInfo(name = "active_ambulances") val activeAmbulances: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
