package com.example.mobiledev.feature.patient.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.repository.ResQRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class PatientHospitalsUiState(
    val hospitals: List<HospitalEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val selectedHospital: HospitalEntity? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && hospitals.isEmpty() && errorMessage == null
}

class PatientHospitalsViewModel(
    private val repository: ResQRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientHospitalsUiState())
    val uiState: StateFlow<PatientHospitalsUiState> = _uiState.asStateFlow()

    private var hospitalsJob: Job? = null

    init {
        observeHospitals()
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        observeHospitals()
    }

    fun onHospitalSelected(hospital: HospitalEntity) {
        _uiState.update { it.copy(selectedHospital = hospital) }
    }

    fun dismissHospitalDetails() {
        _uiState.update { it.copy(selectedHospital = null) }
    }

    private fun observeHospitals() {
        hospitalsJob?.cancel()
        hospitalsJob = viewModelScope.launch {
            repository.getApprovedHospitalsStream()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = throwable.message ?: "Unable to load hospitals"
                        )
                    }
                }
                .collectLatest { hospitals ->
                    _uiState.update { current ->
                        val selectedHospital = current.selectedHospital?.takeIf { selected ->
                            hospitals.any { it.id == selected.id }
                        }
                        current.copy(
                            hospitals = hospitals,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                            selectedHospital = selectedHospital
                        )
                    }
                }
        }
    }
}

class PatientHospitalsViewModelFactory(
    private val repository: ResQRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientHospitalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientHospitalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

