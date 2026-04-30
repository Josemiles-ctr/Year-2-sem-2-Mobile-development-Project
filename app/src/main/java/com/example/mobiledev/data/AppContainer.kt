package com.example.mobiledev.data

import android.content.Context
import com.example.mobiledev.data.repository.ActivityRepository
import com.example.mobiledev.data.repository.FirebaseActivityRepository
import com.example.mobiledev.data.repository.FirebaseDatabaseSeeder
import com.example.mobiledev.data.repository.FirebaseResQRepository
import com.example.mobiledev.data.repository.FirebaseUserRepository
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.repository.LocalEmergencyRepository
import com.example.mobiledev.data.repository.EmergencyRepository
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.data.security.AuthSessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val resQRepository: ResQRepository
    val userRepository: UserRepository
    val authSessionManager: AuthSessionManager
    val emergencyRepository: EmergencyRepository
    val activityRepository: ActivityRepository
}

/**
 * [AppContainer] implementation backed by Firebase repositories.
 */
class AppDataContainer(private val context: Context) : AppContainer {
    
    init {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                FirebaseDatabaseSeeder(firebaseDatabase).seedIfNeeded()
            }
        }
    }

    override val authSessionManager: AuthSessionManager by lazy {
        AuthSessionManager()
    }

    private val firebaseDatabase: FirebaseDatabase by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        FirebaseDatabase.getInstance()
    }

    override val userRepository: UserRepository by lazy {
        FirebaseUserRepository(context, firebaseDatabase)
    }

    /**
     * Implementation for [ResQRepository]
     */
    override val resQRepository: ResQRepository by lazy {
        FirebaseResQRepository(
            firebaseDatabase,
            authSessionManager
        )
    }

    /**
     * Implementation for [EmergencyRepository] using local Room database
     */
    override val emergencyRepository: EmergencyRepository by lazy {
        LocalEmergencyRepository(resQRepository, authSessionManager)
    }

    override val activityRepository: ActivityRepository by lazy {
        FirebaseActivityRepository(firebaseDatabase)
    }
}
