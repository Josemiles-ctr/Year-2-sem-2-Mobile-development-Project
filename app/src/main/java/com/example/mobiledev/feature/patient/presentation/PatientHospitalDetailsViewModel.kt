package com.example.mobiledev.feature.patient.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.repository.ResQRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class PatientHospitalDetailsUiState(
    val hospital: HospitalEntity? = null,
    val ambulances: List<AmbulanceEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class PatientHospitalDetailsViewModel(
    private val repository: ResQRepository,
    private val hospitalId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHospitalDetailsUiState())
    val uiState: StateFlow<PatientHospitalDetailsUiState> = _uiState.asStateFlow()

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
                            _uiState.update {
                                it.copy(
                                    ambulances = ambulances,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
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
}

class PatientHospitalDetailsViewModelFactory(
    private val repository: ResQRepository,
    private val hospitalId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientHospitalDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientHospitalDetailsViewModel(repository, hospitalId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

