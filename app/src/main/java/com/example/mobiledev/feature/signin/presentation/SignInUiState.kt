package com.example.mobiledev.feature.signin.presentation

data class SignInUiState(
    val emailOrPhone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

