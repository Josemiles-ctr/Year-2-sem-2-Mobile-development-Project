package com.example.mobiledev.data

import android.content.Context
import com.example.mobiledev.data.local.ResQDatabase
import com.example.mobiledev.data.repository.OfflineResQRepository
import com.example.mobiledev.data.repository.ResQRepository

/**
 * Dependency Injection container at the application level.
 */
interface AppContainer {
    val resQRepository: ResQRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineResQRepository]
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ResQRepository]
     */
    override val resQRepository: ResQRepository by lazy {
        val database = ResQDatabase.getDatabase(context)
        OfflineResQRepository(
            database.userDao(),
            database.hospitalDao(),
            database.ambulanceDao(),
            database.emergencyRequestDao()
        )
    }
}
