package com.example.mobiledev.feature.signup.presentation

import androidx.lifecycle.ViewModel
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.InMemoryUserRepository
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.domain.validation.Validator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel(
    private val userRepository: UserRepository = InMemoryUserRepository()
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(userRepository.getUsers())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState: StateFlow<SignUpUiState> = _signUpUiState.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> onFullNameChange(event.value)
            is SignUpEvent.PhoneNumberChanged -> onPhoneNumberChange(event.value)
            is SignUpEvent.EmailChanged -> onEmailChange(event.value)
            is SignUpEvent.PasswordChanged -> onPasswordChange(event.value)
            is SignUpEvent.ConfirmPasswordChanged -> onConfirmPasswordChange(event.value)
            SignUpEvent.Submit -> submitSignUp()
            SignUpEvent.ClearFeedback -> clearFeedback()
        }
    }

    fun addUser(name: String, email: String) {
        if (!Validator.isValidName(name)) {
            _errorMessage.value = ERROR_INVALID_NAME
            return
        }
        if (!Validator.isValidEmail(email)) {
            _errorMessage.value = ERROR_INVALID_EMAIL
            return
        }
        userRepository.addUser(name = name.trim(), email = email.trim(), phone = "")
        _users.value = userRepository.getUsers()
        _errorMessage.value = null
    }

    fun onFullNameChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            fullName = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPhoneNumberChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            phoneNumber = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onEmailChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            email = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPasswordChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            password = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            confirmPassword = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun submitSignUp() {
        val state = _signUpUiState.value
        val fullName = state.fullName.trim()
        val phone = state.phoneNumber.trim()
        val email = state.email.trim()

        val error = when {
            !Validator.isValidName(fullName) -> ERROR_INVALID_NAME
            !Validator.isValidPhone(phone) -> ERROR_INVALID_PHONE
            !Validator.isValidEmail(email) -> ERROR_INVALID_EMAIL
            !Validator.isValidPassword(state.password) -> ERROR_INVALID_PASSWORD
            !Validator.passwordsMatch(state.password, state.confirmPassword) -> ERROR_PASSWORD_MISMATCH
            else -> null
        }

        if (error != null) {
            _errorMessage.value = error
            _signUpUiState.value = state.copy(errorMessage = error, successMessage = null)
            return
        }

        userRepository.addUser(name = fullName, email = email, phone = phone)
        _users.value = userRepository.getUsers()
        _errorMessage.value = null
        _signUpUiState.value = SignUpUiState(successMessage = SUCCESS_ACCOUNT_CREATED)
    }

    fun clearFeedback() {
        _errorMessage.value = null
        _signUpUiState.value = _signUpUiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun removeUser(userId: Int) {
        userRepository.removeUser(userId)
        _users.value = userRepository.getUsers()
    }

    private companion object {
        const val ERROR_INVALID_NAME = "Name must be at least 2 characters."
        const val ERROR_INVALID_PHONE = "Phone number must contain 10 to 15 digits."
        const val ERROR_INVALID_EMAIL = "Invalid email address."
        const val ERROR_INVALID_PASSWORD = "Password must be 8+ chars with upper, lower, and number."
        const val ERROR_PASSWORD_MISMATCH = "Passwords do not match."
        const val SUCCESS_ACCOUNT_CREATED = "Account created successfully."
    }
}

