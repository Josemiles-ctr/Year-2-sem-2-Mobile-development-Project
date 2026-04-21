package com.example.mobiledev.feature.signin.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.domain.validation.Validator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val userRepository: UserRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    sealed interface NavigationEvent {
        data object NavigateToDashboard : NavigationEvent
    }

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<NavigationEvent> = _navigationEvents.receiveAsFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailOrPhoneChanged -> onEmailOrPhoneChange(event.value)
            is SignInEvent.PasswordChanged -> onPasswordChange(event.value)
            SignInEvent.Submit -> if (!_uiState.value.isLoading) submitSignIn()
        }
    }

    private fun onEmailOrPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(
            emailOrPhone = value,
            errorMessage = null
        )
    }

    private fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessage = null
        )
    }

    fun submitSignIn() {
        val state = _uiState.value
        val emailOrPhone = state.emailOrPhone.trim()
        val password = state.password

        val isEmailOrPhoneValid = Validator.isValidEmail(emailOrPhone) || Validator.isValidPhone(emailOrPhone)

        val error = when {
            emailOrPhone.isBlank() -> ERROR_MISSING_EMAIL_OR_PHONE
            !isEmailOrPhoneValid -> ERROR_INVALID_EMAIL_OR_PHONE
            password.isBlank() -> ERROR_MISSING_PASSWORD
            else -> null
        }

        if (error != null) {
            _uiState.value = state.copy(isLoading = false, errorMessage = error)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val user = userRepository.authenticateUser(
                    emailOrPhone = emailOrPhone,
                    password = password
                )
                if (user != null) {
                    val role = runCatching { AppRole.valueOf(user.role) }.getOrDefault(AppRole.PATIENT)
                    if (role == AppRole.HOSPITAL_ADMIN) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Use Hospital Admin login for this account."
                        )
                        return@launch
                    }
                    authSessionManager.setPrincipal(
                        AuthPrincipal(
                            userId = user.id,
                            hospitalId = user.hospitalId,
                            role = role
                        )
                    )
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    _navigationEvents.send(NavigationEvent.NavigateToDashboard)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ERROR_INVALID_CREDENTIALS
                    )
                }
            } catch (exception: Exception) {
                val userMessage = toUserMessage(exception)
                Log.e(TAG, "Sign-in failed while accessing Firebase.", exception)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = userMessage)
            }
        }
    }

    private fun toUserMessage(exception: Exception): String {
        val errorText = exception.message?.lowercase().orEmpty()
        return when {
            "permission denied" in errorText || "permission_denied" in errorText -> ERROR_PERMISSION_DENIED
            "network" in errorText || "unable to resolve host" in errorText || "failed to connect" in errorText -> ERROR_NETWORK
            else -> ERROR_DATA_SOURCE
        }
    }

    private companion object {
        const val TAG = "SignInViewModel"
        const val ERROR_MISSING_EMAIL_OR_PHONE = "Email or phone number is required."
        const val ERROR_INVALID_EMAIL_OR_PHONE = "Enter a valid email or phone number."
        const val ERROR_MISSING_PASSWORD = "Password is required."
        const val ERROR_INVALID_CREDENTIALS = "Invalid credentials. Please try again."
        const val ERROR_PERMISSION_DENIED = "Firebase denied access. Check Realtime Database rules."
        const val ERROR_NETWORK = "Network issue while contacting Firebase. Check internet and database URL."
        const val ERROR_DATA_SOURCE = "Could not reach Firebase database. Please try again."
    }
}

class SignInViewModelFactory(
    private val userRepository: UserRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SignInViewModel(userRepository, authSessionManager) as T
    }
}

