package com.example.mobiledev.data.repository

import com.example.mobiledev.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

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
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(
            id = generatedId,
            name = name,
            email = trimmedEmail,
            phone = trimmedPhone,
            password = passwordHash
        )

        val userPayload = mapOf(
            USER_ID_FIELD to user.id,
            USER_NAME_FIELD to user.name,
            USER_EMAIL_FIELD to user.email,
            USER_PHONE_FIELD to user.phone,
            USER_PASSWORD_FIELD to passwordHash,
            USER_EMAIL_KEY_FIELD to trimmedEmail.lowercase(),
            USER_PHONE_KEY_FIELD to normalizePhone(trimmedPhone)
        )
        newUserRef.setValue(userPayload).await()
        return user
    }

    override suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean {
        val credential = emailOrPhone.trim()
        if (credential.isBlank()) return false

        val emailCandidate = credential.lowercase()
        val phoneCandidate = normalizePhone(credential)

        // Use indexed lookups instead of downloading the entire users' collection.
        return findMatchingUserByField(USER_EMAIL_KEY_FIELD, emailCandidate, password) ||
            findMatchingUserByField(USER_PHONE_KEY_FIELD, phoneCandidate, password) ||
            // Legacy fallback for rows created before normalized keys were stored.
            findMatchingUserByField(USER_EMAIL_FIELD, credential, password) ||
            findMatchingUserByField(USER_PHONE_FIELD, credential, password)
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

    private suspend fun findMatchingUserByField(field: String, value: String, password: String): Boolean {
        if (value.isBlank()) return false

        val snapshot = usersRef
            .orderByChild(field)
            .equalTo(value)
            .limitToFirst(1)
            .get()
            .await()

        val matchedSnapshot = snapshot.children.firstOrNull() ?: return false
        val matchedUser = matchedSnapshot.toUserOrNull() ?: return false
        val storedPassword = matchedUser.password

        if (storedPassword.isBlank()) return false

        if (isBcryptHash(storedPassword)) {
            return BCrypt.checkpw(password, storedPassword)
        }

        if (storedPassword != password) return false

        // Migrate legacy plaintext credentials to BCrypt after a successful sign-in.
        val migratedHash = BCrypt.hashpw(password, BCrypt.gensalt())
        runCatching {
            matchedSnapshot.ref.child(USER_PASSWORD_FIELD).setValue(migratedHash).await()
        }
        return true
    }

    private fun normalizePhone(raw: String): String = raw.filter(Char::isDigit)

    private fun isBcryptHash(value: String): Boolean =
        value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$")

    private companion object {
        const val USERS_NODE = "users"
        const val USER_ID_FIELD = "id"
        const val USER_NAME_FIELD = "name"
        const val USER_EMAIL_FIELD = "email"
        const val USER_PHONE_FIELD = "phone"
        const val USER_PASSWORD_FIELD = "password"
        const val USER_EMAIL_KEY_FIELD = "emailKey"
        const val USER_PHONE_KEY_FIELD = "phoneKey"
    }
}

