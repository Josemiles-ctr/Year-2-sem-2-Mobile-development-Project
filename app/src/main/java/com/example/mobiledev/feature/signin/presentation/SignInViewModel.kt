package com.example.mobiledev.feature.signin.presentation

import androidx.lifecycle.ViewModel
import com.example.mobiledev.domain.validation.Validator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignInViewModel : ViewModel() {

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

        _uiState.value = state.copy(errorMessage = null, successMessage = SUCCESS_LOGIN_ACTION)
    }

    private fun clearFeedback() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    private companion object {
        const val ERROR_MISSING_EMAIL_OR_PHONE = "Email or phone number is required."
        const val ERROR_INVALID_EMAIL_OR_PHONE = "Enter a valid email or phone number."
        const val ERROR_MISSING_PASSWORD = "Password is required."
        const val SUCCESS_LOGIN_ACTION = "Login request submitted."
    }
}

