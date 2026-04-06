package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User

class InMemoryUserRepository : UserRepository {
    private val users = mutableListOf<User>()

    override fun getUsers(): List<User> = users.toList()

    override fun addUser(name: String, email: String, phone: String): User {
        val nextId = (users.maxOfOrNull { it.id } ?: 0) + 1
        val user = User(id = nextId, name = name, email = email, phone = phone)
        users += user
        return user
    }

    override fun removeUser(userId: Int) {
        users.removeAll { it.id == userId }
    }
}


