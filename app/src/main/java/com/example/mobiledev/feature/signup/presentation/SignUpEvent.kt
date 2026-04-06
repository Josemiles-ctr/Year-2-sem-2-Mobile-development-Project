package com.example.mobiledev.feature.signup.presentation

sealed interface SignUpEvent {
    data class FullNameChanged(val value: String) : SignUpEvent
    data class PhoneNumberChanged(val value: String) : SignUpEvent
    data class EmailChanged(val value: String) : SignUpEvent
    data class PasswordChanged(val value: String) : SignUpEvent
    data class ConfirmPasswordChanged(val value: String) : SignUpEvent
    data object Submit : SignUpEvent
    data object ClearFeedback : SignUpEvent
}


