package com.example.mobiledev.feature.signup.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.domain.validation.Validator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class SignUpViewModel(
    private val userRepository: UserRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    sealed interface NavigationEvent {
        data object NavigateToDashboard : NavigationEvent
    }

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signUpUiState = MutableStateFlow(SignUpUiState())
    val signUpUiState: StateFlow<SignUpUiState> = _signUpUiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>(capacity = Channel.BUFFERED)
    val navigationEvents: Flow<NavigationEvent> = _navigationEvents.receiveAsFlow()

    init {
        loadUsers()
    }

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.FullNameChanged -> onFullNameChange(event.value)
            is SignUpEvent.PhoneNumberChanged -> onPhoneNumberChange(event.value)
            is SignUpEvent.EmailChanged -> onEmailChange(event.value)
            is SignUpEvent.PasswordChanged -> onPasswordChange(event.value)
            is SignUpEvent.ConfirmPasswordChanged -> onConfirmPasswordChange(event.value)
            SignUpEvent.Submit -> submitSignUp()
        }
    }

    fun onFullNameChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            fullName = value,
            errorMessage = null
        )
    }

    fun onPhoneNumberChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            phoneNumber = value,
            errorMessage = null
        )
    }

    fun onEmailChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            email = value,
            errorMessage = null
        )
    }

    fun onPasswordChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            password = value,
            errorMessage = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _signUpUiState.value = _signUpUiState.value.copy(
            confirmPassword = value,
            errorMessage = null
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
            _signUpUiState.value = state.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            try {
                val user = userRepository.addUser(
                    name = fullName,
                    email = email,
                    phone = phone,
                    password = state.password
                )
                authSessionManager.setPrincipal(
                    AuthPrincipal(
                        userId = user.id,
                        hospitalId = user.hospitalId,
                        role = runCatching { AppRole.valueOf(user.role) }.getOrDefault(AppRole.PATIENT)
                    )
                )
                _errorMessage.value = null
                _signUpUiState.value = state.copy(errorMessage = null)
                _navigationEvents.send(NavigationEvent.NavigateToDashboard)

                // Refresh is the best effort and should not block successful navigation.
                runCatching { refreshUsers() }
                    .onFailure { exception ->
                        Log.w(TAG, "User list refresh failed after successful sign-up.", exception)
                    }
            } catch (exception: Exception) {
                val message = toUserMessage(exception)
                Log.e(TAG, "Sign-up submit failed while accessing Firebase.", exception)
                _errorMessage.value = message
                _signUpUiState.value = state.copy(errorMessage = message)
            }
        }
    }

    fun removeUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeUser(userId)
                refreshUsers()
            } catch (exception: Exception) {
                val message = toUserMessage(exception)
                Log.e(TAG, "removeUser failed while accessing Firebase.", exception)
                _errorMessage.value = message
            }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                refreshUsers()
            } catch (exception: Exception) {
                val message = toUserMessage(exception)
                Log.e(TAG, "Initial user load failed while accessing Firebase.", exception)
                _errorMessage.value = message
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

    private suspend fun refreshUsers() {
        _users.value = userRepository.getUsers()
    }

    private companion object {
        const val TAG = "SignUpViewModel"
        const val ERROR_INVALID_NAME = "Name must be at least 2 characters."
        const val ERROR_INVALID_PHONE = "Phone number must contain 10 to 15 digits."
        const val ERROR_INVALID_EMAIL = "Invalid email address."
        const val ERROR_INVALID_PASSWORD = "Password must be 8+ chars with upper, lower, and number."
        const val ERROR_PASSWORD_MISMATCH = "Passwords do not match."
        const val ERROR_PERMISSION_DENIED = "Firebase denied access. Check Realtime Database rules."
        const val ERROR_NETWORK = "Network issue while contacting Firebase. Check internet and database URL."
        const val ERROR_DATA_SOURCE = "Could not reach Firebase database. Please try again."
    }
}

class SignUpViewModelFactory(
    private val userRepository: UserRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SignUpViewModel(userRepository, authSessionManager) as T
    }
}

