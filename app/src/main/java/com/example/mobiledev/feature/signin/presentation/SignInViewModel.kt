package com.example.mobiledev.feature.signin.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.repository.FirebaseUserRepository
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.domain.validation.Validator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val userRepository: UserRepository = FirebaseUserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailOrPhoneChanged -> onEmailOrPhoneChange(event.value)
            is SignInEvent.PasswordChanged -> onPasswordChange(event.value)
            SignInEvent.Submit -> submitSignIn()
            SignInEvent.ClearFeedback -> clearFeedback()
        }
    }

    private fun onEmailOrPhoneChange(value: String) {
        _uiState.value = _uiState.value.copy(
            emailOrPhone = value,
            errorMessage = null,
            successMessage = null
        )
    }

    private fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessage = null,
            successMessage = null
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
            _uiState.value = state.copy(errorMessage = error, successMessage = null)
            return
        }

        viewModelScope.launch {
            try {
                val isAuthenticated = userRepository.authenticateUser(
                    emailOrPhone = emailOrPhone,
                    password = password
                )
                _uiState.value = if (isAuthenticated) {
                    state.copy(errorMessage = null, successMessage = SUCCESS_LOGIN_ACTION)
                } else {
                    state.copy(errorMessage = ERROR_INVALID_CREDENTIALS, successMessage = null)
                }
            } catch (exception: Exception) {
                val userMessage = toUserMessage(exception)
                Log.e(TAG, "Sign-in failed while accessing Firebase.", exception)
                _uiState.value = state.copy(errorMessage = userMessage, successMessage = null)
            }
        }
    }

    private fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
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
        const val SUCCESS_LOGIN_ACTION = "Login successful."
    }
}

