package com.example.mobiledev.feature.hospital.presentation

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity

data class HospitalDashboardUiState(
    val hospitalName: String = "",
    val activeRequests: List<EmergencyRequestEntity> = emptyList(),
    val availableAmbulances: List<AmbulanceEntity> = emptyList(),
    val selectedRequest: EmergencyRequestEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
