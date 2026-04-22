package com.example.mobiledev.data

import android.content.Context
import com.example.mobiledev.data.local.ResQDatabase
import com.example.mobiledev.data.repository.OfflineResQRepository
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.repository.LocalEmergencyRepository
import com.example.mobiledev.data.repository.EmergencyRepository
import com.example.mobiledev.data.security.AuthSessionManager

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val resQRepository: ResQRepository
    val authSessionManager: AuthSessionManager
    val emergencyRepository: EmergencyRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineResQRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val authSessionManager: AuthSessionManager by lazy {
        AuthSessionManager()
    }

    /**
     * Implementation for [ResQRepository]
     */
    override val resQRepository: ResQRepository by lazy {
        val database = ResQDatabase.getDatabase(context)
        OfflineResQRepository(
            database.userDao(),
            database.hospitalDao(),
            database.ambulanceDao(),
            database.emergencyRequestDao(),
            authSessionManager
        )
    }

    /**
     * Implementation for [EmergencyRepository] using local Room database
     */
    override val emergencyRepository: EmergencyRepository by lazy {
        LocalEmergencyRepository(resQRepository, authSessionManager)
    }
}
