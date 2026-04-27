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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


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
    private val authSessionManager: AuthSessionManager,
    private val pollingEnabled: Boolean = true,
    private val pollingIntervalMs: Long = 30_000L
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHospitalDetailsUiState())
    val uiState: StateFlow<PatientHospitalDetailsUiState> = _uiState.asStateFlow()

    private var ambulanceStreamJob: Job? = null
    private var pollingJob: Job? = null

    init {
        loadHospitalDetails()
        if (pollingEnabled) {
            startPolling()
        }
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

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(pollingIntervalMs)
                pollLatestHospitalData()
            }
        }
    }

    private suspend fun pollLatestHospitalData() {
        runCatching {
            val hospital = repository.getHospitalById(hospitalId)
            val ambulances = repository.getAmbulancesByHospitalStream(hospitalId).first()
            hospital to ambulances
        }.onSuccess { (hospital, ambulances) ->
            if (hospital != null) {
                _uiState.update { it.copy(hospital = hospital, errorMessage = null) }
                updateAmbulanceState(ambulances)
            }
        }.onFailure { throwable ->
            _uiState.update {
                it.copy(errorMessage = throwable.message ?: "Unable to refresh ambulance status")
            }
        }
    }

    @Suppress("unused")
    fun submitEmergencyRequest(description: String) {
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
            val request = EmergencyRequestEntity(
                id = "REQ_$now",
                userId = patientId,
                hospitalId = hospitalId,
                ambulanceId = null,
                status = "PENDING",
                description = trimmedDescription,
                location = hospital.location,
                latitude = hospital.latitude,
                longitude = hospital.longitude,
                priority = "HIGH",
                estimatedTimeMins = null,
                createdAt = now,
                updatedAt = now,
                completedAt = null,
                isDeleted = false
            )

            runCatching { repository.insertRequest(request) }
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
        pollingJob?.cancel()
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

