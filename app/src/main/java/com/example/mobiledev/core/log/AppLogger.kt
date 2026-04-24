package com.example.mobiledev.core.log

import android.util.Log
import com.example.mobiledev.BuildConfig
// import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * A centralized logging utility that bridges local logging and remote crash reporting.
 */
object AppLogger {

    /**
     * Logs an error message and optionally sends a non-fatal report to Crashlytics.
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            // In production, we log to Crashlytics
            // FirebaseCrashlytics.getInstance().log("$tag: $message")
            // throwable?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        }
    }

    /**
     * Logs an info message.
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        } else {
            // FirebaseCrashlytics.getInstance().log("$tag: $message")
        }
    }

    /**
     * Logs a warning message.
     */
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        } else {
            // FirebaseCrashlytics.getInstance().log("$tag: $message")
        }
    }
}
