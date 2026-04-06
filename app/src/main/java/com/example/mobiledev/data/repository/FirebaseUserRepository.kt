package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepository {

    private val usersRef = firebaseDatabase.getReference(USERS_NODE)

    override suspend fun getUsers(): List<User> {
        val snapshot = usersRef.get().await()
        return snapshot.children.mapNotNull { child ->
            child.toUserOrNull()
        }
    }

    override suspend fun addUser(name: String, email: String, phone: String, password: String): User {
        val newUserRef = usersRef.push()
        val generatedId = newUserRef.key.orEmpty()
        val user = User(id = generatedId, name = name, email = email, phone = phone, password = password)
        newUserRef.setValue(user).await()
        return user
    }

    override suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean {
        val credential = emailOrPhone.trim()
        if (credential.isBlank()) return false

        val snapshot = usersRef.get().await()
        return snapshot.children.mapNotNull { it.toUserOrNull() }.any { user ->
            val emailMatches = user.email.equals(credential, ignoreCase = true)
            val phoneMatches = user.phone == credential
            val passwordMatches = user.password == password
            (emailMatches || phoneMatches) && passwordMatches
        }
    }

    override suspend fun removeUser(userId: String) {
        if (userId.isBlank()) return
        usersRef.child(userId).removeValue().await()
    }

    private fun DataSnapshot.toUserOrNull(): User? {
        val value = getValue(User::class.java) ?: return null
        return if (value.id.isBlank() && key != null) {
            value.copy(id = key.orEmpty())
        } else {
            value
        }
    }

    private companion object {
        const val USERS_NODE = "users"
    }
}

