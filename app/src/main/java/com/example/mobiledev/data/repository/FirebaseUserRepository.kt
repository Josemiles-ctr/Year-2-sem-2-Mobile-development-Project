package com.example.mobiledev.data.repository

import android.content.Context
import com.example.mobiledev.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

    override suspend fun getUsers(): List<User> = withContext(Dispatchers.IO) {
        val snapshot = usersRef.get().await()
        snapshot.children.mapNotNull { child ->
            child.toUserOrNull()
        }
    }

    override suspend fun addUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        hospitalId: String?,
        accountStatus: String
    ): User {
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
            password = passwordHash,
            role = role,
            hospitalId = hospitalId,
            accountStatus = accountStatus
        )

        val userPayload = mapOf(
            USER_ID_FIELD to user.id,
            USER_NAME_FIELD to user.name,
            USER_EMAIL_FIELD to user.email,
            USER_PHONE_FIELD to user.phone,
            USER_PASSWORD_FIELD to passwordHash,
            USER_ROLE_FIELD to user.role,
            USER_TYPE_FIELD to user.role,
            USER_HOSPITAL_ID_FIELD to user.hospitalId,
            USER_LOCATION_FIELD to "Unknown",
            USER_ACCOUNT_STATUS_FIELD to user.accountStatus,
            USER_CREATED_AT_FIELD to System.currentTimeMillis(),
            USER_UPDATED_AT_FIELD to System.currentTimeMillis(),
            USER_EMAIL_KEY_FIELD to normalizedEmail,
            USER_PHONE_KEY_FIELD to normalizedPhone
        )
        newUserRef.setValue(userPayload).await()
        return user
    }

    override suspend fun authenticateUser(emailOrPhone: String, password: String): User? {
        val credential = emailOrPhone.trim()
        if (credential.isBlank()) return null

        val emailCandidate = credential.lowercase()
        val phoneCandidate = normalizePhone(credential)

        val matchedUser = getUsers().firstOrNull { user ->
            val emailMatches = emailCandidate.isNotBlank() && normalizeEmail(user.email) == emailCandidate
            val phoneMatches = phoneCandidate.isNotBlank() && normalizePhone(user.phone) == phoneCandidate
            val keyMatches = (emailCandidate.isNotBlank() && user.emailKey == emailCandidate) ||
                             (phoneCandidate.isNotBlank() && user.phoneKey == phoneCandidate)
            val exactMatches = user.email.equals(credential, ignoreCase = true) || user.phone == credential
            
            emailMatches || phoneMatches || keyMatches || exactMatches
        } ?: return null

        return if (verifyPasswordAndMigrateIfNeeded(matchedUser, password)) matchedUser else null
    }

    override suspend fun authenticateHospital(email: String, password: String): User? {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) return null

        val matchedUser = getUsers().firstOrNull { user ->
            normalizeEmail(user.email) == trimmedEmail &&
                user.role == HOSPITAL_ROLE &&
                user.accountStatus == APPROVED_STATUS
        } ?: return null

        return if (verifyPasswordAndMigrateIfNeeded(matchedUser, password)) matchedUser else null
    }

    override suspend fun removeUser(userId: String) {
        if (userId.isBlank()) return
        usersRef.child(userId).removeValue().await()
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userPayload = mutableMapOf<String, Any?>(
                USER_ID_FIELD to user.id,
                USER_NAME_FIELD to user.name,
                USER_EMAIL_FIELD to user.email,
                USER_PHONE_FIELD to user.phone,
                USER_ROLE_FIELD to user.role,
                USER_TYPE_FIELD to user.role,
                USER_HOSPITAL_ID_FIELD to user.hospitalId,
                USER_LOCATION_FIELD to "Unknown",
                USER_ACCOUNT_STATUS_FIELD to user.accountStatus,
                USER_UPDATED_AT_FIELD to System.currentTimeMillis(),
                USER_EMAIL_KEY_FIELD to normalizeEmail(user.email),
                USER_PHONE_KEY_FIELD to normalizePhone(user.phone)
            )
            // Only update password if it's not empty and it's a new hash
            if (user.password.isNotBlank() && isBcryptHash(user.password)) {
                userPayload[USER_PASSWORD_FIELD] = user.password
            }
            
            usersRef.child(user.id).updateChildren(userPayload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        const val USER_ROLE_FIELD = "role"
        const val USER_TYPE_FIELD = "userType"
        const val USER_HOSPITAL_ID_FIELD = "hospitalId"
        const val USER_LOCATION_FIELD = "location"
        const val USER_ACCOUNT_STATUS_FIELD = "accountStatus"
        const val USER_CREATED_AT_FIELD = "createdAt"
        const val USER_UPDATED_AT_FIELD = "updatedAt"
        const val USER_EMAIL_KEY_FIELD = "emailKey"
        const val USER_PHONE_KEY_FIELD = "phoneKey"
        const val HOSPITAL_ROLE = "HOSPITAL_ADMIN"
        const val APPROVED_STATUS = "APPROVED"
    }
}
