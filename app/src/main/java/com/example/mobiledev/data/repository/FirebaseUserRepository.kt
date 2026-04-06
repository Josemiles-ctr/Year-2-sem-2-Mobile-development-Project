package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) : UserRepository {

    private val usersRef = firebaseDatabase.getReference(USERS_NODE)

    override suspend fun getUsers(): List<User> {
        val snapshot = usersRef.get().await()
        return snapshot.children.mapNotNull { child ->
            child.toUserOrNull()
        }
    }

    override suspend fun addUser(name: String, email: String, phone: String): User {
        val newUserRef = usersRef.push()
        val generatedId = newUserRef.key.orEmpty()
        val user = User(id = generatedId, name = name, email = email, phone = phone)
        newUserRef.setValue(user).await()
        return user
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

