package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()

    override suspend fun getUsers(): List<User> = users.toList()

    override suspend fun addUser(name: String, email: String, phone: String, password: String): User {
        val nextId = (users.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        val user = User(id = nextId.toString(), name = name, email = email, phone = phone, password = password)
        users += user
        return user
    }

    override suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean {
        val credential = emailOrPhone.trim()
        return users.any { user ->
            (user.email.equals(credential, ignoreCase = true) || user.phone == credential) &&
                user.password == password
        }
    }

    override suspend fun removeUser(userId: String) {
        users.removeAll { it.id == userId }
    }
}


