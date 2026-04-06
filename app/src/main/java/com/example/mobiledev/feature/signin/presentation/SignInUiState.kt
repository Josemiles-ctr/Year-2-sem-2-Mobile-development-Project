package com.example.mobiledev.feature.signin.presentation

data class SignInUiState(
    val emailOrPhone: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

