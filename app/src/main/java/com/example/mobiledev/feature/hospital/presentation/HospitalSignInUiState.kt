package com.example.mobiledev.feature.hospital.presentation

data class HospitalSignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val jwtToken: String? = null
)
