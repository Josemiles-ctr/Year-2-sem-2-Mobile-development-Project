package com.example.mobiledev.feature.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.location.Location
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.security.AuthSessionManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CrisisSubmissionUiState(
    val selectedIncidentType: String? = null,
    val description: String = "",
    val address: String = "Detecting location...",
    val userLocation: LatLng = LatLng(-1.286389, 36.817223), // Nairobi default
    val hospitals: List<HospitalEntity> = emptyList(),
    val ambulances: List<AmbulanceEntity> = emptyList(),
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

class CrisisSubmissionViewModel(
    private val repository: ResQRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrisisSubmissionUiState())
    val uiState: StateFlow<CrisisSubmissionUiState> = _uiState.asStateFlow()

    init {
        observeNearbyEntities()
    }

    private fun observeNearbyEntities() {
        viewModelScope.launch {
            repository.getApprovedHospitalsStream().collect { hospitals ->
                _uiState.update { it.copy(hospitals = hospitals) }
            }
        }
        viewModelScope.launch {
            repository.getAllAvailableAmbulancesStream().collect { ambulances ->
                _uiState.update { it.copy(ambulances = ambulances) }
            }
        }
    }

    fun onIncidentTypeSelected(type: String) {
        _uiState.update { it.copy(selectedIncidentType = type) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateLocation(latLng: LatLng, address: String) {
        _uiState.update { it.copy(userLocation = latLng, address = address) }
    }

    fun submitRequest() {
        val state = _uiState.value
        val type = state.selectedIncidentType
        if (type == null) {
            _uiState.update { it.copy(error = "Please select an incident type") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val userId = authSessionManager.currentPrincipal.userId ?: return@launch
            
            // Find the nearest approved hospital to send the request to
            val hospitals = repository.getApprovedHospitalsStream().firstOrNull() ?: emptyList()
            val nearestHospital = hospitals.minByOrNull { hospital ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    state.userLocation.latitude, state.userLocation.longitude,
                    hospital.latitude ?: 0.0, hospital.longitude ?: 0.0,
                    results
                )
                results[0]
            }
            val hospitalId = nearestHospital?.id
            
            if (hospitalId == null) {
                _uiState.update { it.copy(isSubmitting = false, error = "No hospitals available at the moment.") }
                return@launch
            }

            val now = System.currentTimeMillis()
            val request = EmergencyRequestEntity(
                id = "REQ_${UUID.randomUUID()}",
                userId = userId,
                hospitalId = hospitalId,
                ambulanceId = null,
                status = "PENDING",
                description = "[$type] ${state.description}",
                location = state.address,
                latitude = state.userLocation.latitude,
                longitude = state.userLocation.longitude,
                priority = "HIGH",
                estimatedTimeMins = null,
                createdAt = now,
                updatedAt = now,
                completedAt = null,
                isDeleted = false
            )

            runCatching { repository.insertRequest(request) }
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, error = e.message) }
                }
        }
    }

    fun resetState() {
        _uiState.update { CrisisSubmissionUiState() }
    }
}

class CrisisSubmissionViewModelFactory(
    private val repository: ResQRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CrisisSubmissionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CrisisSubmissionViewModel(repository, authSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
