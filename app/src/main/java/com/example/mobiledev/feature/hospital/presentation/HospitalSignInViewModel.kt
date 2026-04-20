package com.example.mobiledev.feature.hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.repository.ResQRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HospitalSignInViewModel(
    private val repository: ResQRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HospitalSignInUiState())
    val uiState: StateFlow<HospitalSignInUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onSignInClick() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val hospital = repository.loginHospital(email, password)
            
            if (hospital != null) {
                when (hospital.status) {
                    "APPROVED" -> {
                        val token = "jwt_token_hospital_${hospital.id}"
                        _uiState.update { it.copy(isLoading = false, jwtToken = token) }
                        _navigationEvents.emit(NavigationEvent.NavigateToDashboard(hospital.id))
                    }
                    "PENDING" -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Hospital pending verification") }
                    }
                    "REJECTED" -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Hospital account rejected") }
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Unknown account status") }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Invalid email or password") }
            }
        }
    }

    sealed interface NavigationEvent {
        data class NavigateToDashboard(val hospitalId: String) : NavigationEvent
    }
}

class HospitalSignInViewModelFactory(
    private val repository: ResQRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HospitalSignInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HospitalSignInViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
