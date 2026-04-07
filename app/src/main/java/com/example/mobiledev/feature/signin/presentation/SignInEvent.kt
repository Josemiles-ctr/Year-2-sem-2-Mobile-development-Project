package com.example.mobiledev.feature.signin.presentation

sealed interface SignInEvent {
    data class EmailOrPhoneChanged(val value: String) : SignInEvent
    data class PasswordChanged(val value: String) : SignInEvent
    data object Submit : SignInEvent
}

