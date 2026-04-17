package com.example.mobiledev

import android.app.Application
import com.example.mobiledev.data.AppContainer
import com.example.mobiledev.data.AppDataContainer

class ResQApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
