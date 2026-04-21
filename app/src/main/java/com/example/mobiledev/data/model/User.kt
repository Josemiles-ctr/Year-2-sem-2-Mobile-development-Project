package com.example.mobiledev.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "PATIENT",
    val hospitalId: String? = null,
    val accountStatus: String = "ACTIVE",
    val emailKey: String = "",
    val phoneKey: String = ""
)

