package com.example.mobiledev.data.repository

import android.content.Context
import com.example.mobiledev.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    init {
        maybeSeedHospitalAccounts()
    }

    override suspend fun getUsers(): List<User> {
        val snapshot = usersRef.get().await()
        return snapshot.children.mapNotNull { child ->
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
            USER_HOSPITAL_ID_FIELD to user.hospitalId,
            USER_ACCOUNT_STATUS_FIELD to user.accountStatus,
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

    private fun maybeSeedHospitalAccounts() {
        synchronized(FirebaseUserRepository::class.java) {
            if (seedTriggered) return
            seedTriggered = true
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                seedHospitalAccounts()
            }
        }
    }

    private suspend fun seedHospitalAccounts() {
        val seedPasswordHash = BCrypt.hashpw("password123", BCrypt.gensalt())
        val seedAccounts = listOf(
            User("ADMIN_1", "Alice Admin", "hospital1@resq.local", "555-1001", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_1", APPROVED_STATUS),
            User("ADMIN_2", "Brian Admin", "hospital2@resq.local", "555-1002", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_2", APPROVED_STATUS),
            User("ADMIN_3", "Carmen Admin", "hospital3@resq.local", "555-1003", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_3", APPROVED_STATUS),
            User("ADMIN_4", "Daniel Admin", "hospital4@resq.local", "555-1004", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_4", APPROVED_STATUS),
            User("ADMIN_5", "Eva Admin", "hospital5@resq.local", "555-1005", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_5", PENDING_STATUS),
            User("ADMIN_6", "Farah Admin", "hospital6@resq.local", "555-1006", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_6", APPROVED_STATUS),
            User("ADMIN_7", "George Admin", "hospital7@resq.local", "555-1007", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_7", REJECTED_STATUS),
            User("ADMIN_8", "Helen Admin", "hospital8@resq.local", "555-1008", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_8", APPROVED_STATUS),
            User("ADMIN_9", "Ian Admin", "hospital9@resq.local", "555-1009", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_9", APPROVED_STATUS),
            User("ADMIN_10", "Julia Admin", "hospital10@resq.local", "555-1010", seedPasswordHash, HOSPITAL_ROLE, "HOSPITAL_10", APPROVED_STATUS)
        )

        seedAccounts.forEach { seedUser ->
            val payload = mapOf(
                USER_ID_FIELD to seedUser.id,
                USER_NAME_FIELD to seedUser.name,
                USER_EMAIL_FIELD to seedUser.email,
                USER_PHONE_FIELD to seedUser.phone,
                USER_PASSWORD_FIELD to seedUser.password,
                USER_ROLE_FIELD to seedUser.role,
                USER_HOSPITAL_ID_FIELD to seedUser.hospitalId,
                USER_ACCOUNT_STATUS_FIELD to seedUser.accountStatus,
                USER_EMAIL_KEY_FIELD to normalizeEmail(seedUser.email),
                USER_PHONE_KEY_FIELD to normalizePhone(seedUser.phone)
            )
            usersRef.child(seedUser.id).setValue(payload).await()
        }
    }

    private companion object {
        const val USERS_NODE = "users"
        const val USER_ID_FIELD = "id"
        const val USER_NAME_FIELD = "name"
        const val USER_EMAIL_FIELD = "email"
        const val USER_PHONE_FIELD = "phone"
        const val USER_PASSWORD_FIELD = "password"
        const val USER_ROLE_FIELD = "role"
        const val USER_HOSPITAL_ID_FIELD = "hospitalId"
        const val USER_ACCOUNT_STATUS_FIELD = "accountStatus"
        const val USER_EMAIL_KEY_FIELD = "emailKey"
        const val USER_PHONE_KEY_FIELD = "phoneKey"
        const val HOSPITAL_ROLE = "HOSPITAL_ADMIN"
        const val APPROVED_STATUS = "APPROVED"
        const val PENDING_STATUS = "PENDING"
        const val REJECTED_STATUS = "REJECTED"

        @Volatile
        private var seedTriggered: Boolean = false
    }
}
