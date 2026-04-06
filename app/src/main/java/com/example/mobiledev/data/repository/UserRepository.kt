package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun addUser(name: String, email: String, phone: String, password: String): User
    suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean
    suspend fun removeUser(userId: String)
}


