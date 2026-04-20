package com.example.mobiledev.data.repository

import android.content.Context
import com.example.mobiledev.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

class FirebaseUserRepository(
    context: Context,
    firebaseDatabase: FirebaseDatabase? = null
) : UserRepository {

    private val db: FirebaseDatabase by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        firebaseDatabase ?: FirebaseDatabase.getInstance()
    }

    private val usersRef by lazy { db.getReference(USERS_NODE) }

    override suspend fun getUsers(): List<User> {
        val snapshot = usersRef.get().await()
        return snapshot.children.mapNotNull { child ->
            child.toUserOrNull()
        }
    }

    override suspend fun addUser(name: String, email: String, phone: String, password: String): User {
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val normalizedEmail = trimmedEmail.lowercase()
        val normalizedPhone = normalizePhone(trimmedPhone)

        val existingUsers = getUsers()
        val duplicateExists = existingUsers.any { user ->
            normalizeEmail(user.email) == normalizedEmail || normalizePhone(user.phone) == normalizedPhone
        }
        if (duplicateExists) {
            throw IllegalStateException("An account with this email or phone already exists.")
        }

        val newUserRef = usersRef.push()
        val generatedId = newUserRef.key.orEmpty()
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
            USER_EMAIL_KEY_FIELD to normalizedEmail,
            USER_PHONE_KEY_FIELD to normalizedPhone
        )
        newUserRef.setValue(userPayload).await()
        return user
    }

    override suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean {
        val credential = emailOrPhone.trim()
        if (credential.isBlank()) return false

        val emailCandidate = credential.lowercase()
        val phoneCandidate = normalizePhone(credential)

        val matchedUser = getUsers().firstOrNull { user ->
            normalizeEmail(user.email) == emailCandidate || normalizePhone(user.phone) == phoneCandidate ||
                user.emailKey == emailCandidate || user.phoneKey == phoneCandidate ||
                user.email.equals(credential, ignoreCase = true) || user.phone == credential
        } ?: return false

        return verifyPasswordAndMigrateIfNeeded(matchedUser, password)
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

    private suspend fun verifyPasswordAndMigrateIfNeeded(matchedUser: User, password: String): Boolean {
        val storedPassword = matchedUser.password

        if (storedPassword.isBlank()) return false

        if (isBcryptHash(storedPassword)) {
            return BCrypt.checkpw(password, storedPassword)
        }

        if (storedPassword != password) return false

        val migratedHash = BCrypt.hashpw(password, BCrypt.gensalt())
        runCatching {
            if (matchedUser.id.isNotBlank()) {
                usersRef.child(matchedUser.id).child(USER_PASSWORD_FIELD).setValue(migratedHash).await()
            }
        }
        return true
    }

    private fun normalizeEmail(raw: String): String = raw.trim().lowercase()

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
