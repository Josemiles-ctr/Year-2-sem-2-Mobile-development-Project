package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User

interface UserRepository {
    fun getUsers(): List<User>
    fun addUser(name: String, email: String, phone: String): User
    fun removeUser(userId: Int)
}


