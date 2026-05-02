package com.example.mobiledev.feature.patient.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.security.AuthSessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID


data class PatientHospitalDetailsUiState(
    val hospital: HospitalEntity? = null,
    val ambulances: List<AmbulanceEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val availableAmbulanceCount: Int = 0,
    val isHospitalOffline: Boolean = false,
    val isSubmittingRequest: Boolean = false,
    val submitErrorMessage: String? = null,
    val submitSuccessMessage: String? = null,
    val lastUpdatedAtMillis: Long? = null
)

class PatientHospitalDetailsViewModel(
    private val repository: ResQRepository,
    private val hospitalId: String,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHospitalDetailsUiState())
    val uiState: StateFlow<PatientHospitalDetailsUiState> = _uiState.asStateFlow()

    private var ambulanceStreamJob: Job? = null

    init {
        loadHospitalDetails()
    }

    private fun loadHospitalDetails() {
        viewModelScope.launch {
            runCatching { repository.getHospitalById(hospitalId) }
                .onSuccess { hospital ->
                    if (hospital == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Hospital not found"
                            )
                        }
                        return@launch
                    }

                    _uiState.update {
                        it.copy(
                            hospital = hospital,
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    observeAmbulances()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load hospital details"
                        )
                    }
                }
        }
    }

    private fun observeAmbulances() {
        ambulanceStreamJob?.cancel()
        ambulanceStreamJob = viewModelScope.launch {
            repository.getAmbulancesByHospitalStream(hospitalId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load ambulances"
                        )
                    }
                }
                .collectLatest { ambulances ->
                    // Also refresh the hospital data to get updated activeAmbulances count
                    val updatedHospital = repository.getHospitalById(hospitalId)
                    _uiState.update { it.copy(hospital = updatedHospital ?: it.hospital) }
                    updateAmbulanceState(ambulances)
                }
        }
    }

    private fun updateAmbulanceState(ambulances: List<AmbulanceEntity>) {
        val availableAmbulances = ambulances.count { it.status.equals("AVAILABLE", ignoreCase = true) }
        val isHospitalOffline = ambulances.isNotEmpty() && ambulances.all {
            it.status.equals("OFFLINE", ignoreCase = true)
        }

        _uiState.update {
            it.copy(
                ambulances = ambulances,
                availableAmbulanceCount = availableAmbulances,
                isHospitalOffline = isHospitalOffline,
                isLoading = false,
                errorMessage = null,
                lastUpdatedAtMillis = System.currentTimeMillis()
            )
        }
    }

    fun submitEmergencyRequest(
        description: String,
        patientLatitude: Double? = null,
        patientLongitude: Double? = null,
        ambulanceId: String? = null
    ) {
        val patientId = authSessionManager.currentPrincipal.userId
        val hospital = _uiState.value.hospital
        val trimmedDescription = description.trim()

        when {
            patientId.isNullOrBlank() -> {
                _uiState.update {
                    it.copy(
                        isSubmittingRequest = false,
                        submitErrorMessage = "Patient session expired. Please sign in again.",
                        submitSuccessMessage = null
                    )
                }
                return
            }
            hospital == null -> {
                _uiState.update {
                    it.copy(
                        isSubmittingRequest = false,
                        submitErrorMessage = "Hospital details are unavailable.",
                        submitSuccessMessage = null
                    )
                }
                return
            }
            trimmedDescription.isBlank() -> {
                _uiState.update {
                    it.copy(
                        isSubmittingRequest = false,
                        submitErrorMessage = "Please add a short emergency description.",
                        submitSuccessMessage = null
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingRequest = true,
                    submitErrorMessage = null,
                    submitSuccessMessage = null
                )
            }

            val now = System.currentTimeMillis()
            val hasPatientLocation = patientLatitude != null && patientLongitude != null
            val request = EmergencyRequestEntity(
                id = "REQ_${UUID.randomUUID()}",
                userId = patientId,
                hospitalId = hospitalId,
                ambulanceId = ambulanceId,
                status = "PENDING",
                description = trimmedDescription,
                location = if (hasPatientLocation) {
                    String.format("%.6f, %.6f", patientLatitude!!, patientLongitude!!)
                } else {
                    hospital.location
                },
                latitude = patientLatitude ?: hospital.latitude,
                longitude = patientLongitude ?: hospital.longitude,
                priority = "HIGH",
                estimatedTimeMins = null,
                createdAt = now,
                updatedAt = now,
                completedAt = null,
                isDeleted = false
            )

            runCatching { 
                repository.insertRequest(request)
                if (ambulanceId != null) {
                    repository.getAmbulanceById(ambulanceId)?.let { ambulance ->
                        repository.updateAmbulance(ambulance.copy(status = "ON_EMERGENCY"))
                    }
                }
            }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmittingRequest = false,
                            submitErrorMessage = null,
                            submitSuccessMessage = "Emergency request submitted successfully."
                        )
                    }
                }
                .onFailure { throwable ->
                    val message = throwable.message.orEmpty()
                    val friendlyMessage = if (
                        message.contains("FOREIGN KEY", ignoreCase = true) ||
                        message.contains("SQLITE_CONSTRAINT", ignoreCase = true)
                    ) {
                        "Unable to submit the request with the current account context. Please try signing in again."
                    } else {
                        throwable.message ?: "Unable to submit emergency request"
                    }
                    _uiState.update {
                        it.copy(
                            isSubmittingRequest = false,
                            submitSuccessMessage = null,
                            submitErrorMessage = friendlyMessage
                        )
                    }
                }
        }
    }

    @Suppress("unused")
    fun clearSubmitMessage() {
        _uiState.update {
            it.copy(
                submitErrorMessage = null,
                submitSuccessMessage = null
            )
        }
    }

    override fun onCleared() {
        ambulanceStreamJob?.cancel()
        super.onCleared()
    }
}

class PatientHospitalDetailsViewModelFactory(
    private val repository: ResQRepository,
    private val hospitalId: String,
    private val authSessionManager: AuthSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientHospitalDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientHospitalDetailsViewModel(repository, hospitalId, authSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

