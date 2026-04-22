package com.example.mobiledev.feature.hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.repository.ResQRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HospitalDashboardViewModel(
    private val repository: ResQRepository,
    private val hospitalId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(HospitalDashboardUiState())
    val uiState: StateFlow<HospitalDashboardUiState> = _uiState.asStateFlow()

    init {
        if (hospitalId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Invalid hospital session. Please sign in again."
                )
            }
        } else {
            loadDashboardData()
            observeAmbulances()
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val hospital = repository.getHospitalById(hospitalId)
                _uiState.update { it.copy(hospitalName = hospital?.name ?: "Hospital Dashboard") }

                repository.getRequestsByHospitalStream(hospitalId)
                    .onEach { requests ->
                        _uiState.update { it.copy(activeRequests = requests, isLoading = false) }
                    }
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                    .collect()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load hospital dashboard"
                    )
                }
            }
        }
    }

    private fun observeAmbulances() {
        viewModelScope.launch {
            try {
                repository.getAvailableAmbulancesStream(hospitalId)
                    .onEach { ambulances ->
                        _uiState.update { it.copy(availableAmbulances = ambulances) }
                    }
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message) }
                    }
                    .collect()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load available ambulances")
                }
            }
        }
    }

    fun onRequestSelected(request: EmergencyRequestEntity?) {
        _uiState.update { it.copy(selectedRequest = request) }
    }

    fun assignAmbulance(requestId: String, ambulanceId: String) {
        viewModelScope.launch {
            try {
                val request = repository.getRequestById(requestId)
                val ambulance = repository.getAmbulanceById(ambulanceId)

                if (request != null && ambulance != null) {
                    val updatedRequest = request.copy(
                        ambulanceId = ambulanceId,
                        status = "ASSIGNED",
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateRequest(updatedRequest)

                    val updatedAmbulance = ambulance.copy(
                        status = "BUSY",
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateAmbulance(updatedAmbulance)

                    onRequestSelected(null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to assign ambulance")
                }
            }
        }
    }

    fun addAmbulance(registrationNo: String, driverId: String) {
        viewModelScope.launch {
            try {
                val newAmbulance = com.example.mobiledev.data.local.entity.AmbulanceEntity(
                    id = "AMB_${System.currentTimeMillis()}",
                    hospitalId = hospitalId,
                    driverId = driverId,
                    registrationNo = registrationNo,
                    licenseNo = "LIC-${System.currentTimeMillis()}",
                    status = "AVAILABLE",
                    latitude = 0.0,
                    longitude = 0.0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertAmbulance(newAmbulance)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to add ambulance")
                }
            }
        }
    }
}

class HospitalDashboardViewModelFactory(
    private val repository: ResQRepository,
    private val hospitalId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HospitalDashboardViewModel(repository, hospitalId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
