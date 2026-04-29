package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun addUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String = "PATIENT",
        hospitalId: String? = null,
        accountStatus: String = "ACTIVE"
    ): User
    suspend fun authenticateUser(emailOrPhone: String, password: String): User?
    suspend fun authenticateHospital(email: String, password: String): User?
    suspend fun removeUser(userId: String)
    suspend fun updateUser(user: User): Result<Unit>
}


