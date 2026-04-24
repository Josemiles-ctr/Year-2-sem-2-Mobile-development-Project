package com.example.mobiledev.core.base

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mobiledev.core.error.AppError
import com.example.mobiledev.core.log.AppLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * A base ViewModel that provides centralized error handling and logging capabilities.
 * All feature ViewModels should extend this class to ensure consistent error management.
 */
abstract class BaseViewModel : ViewModel() {

    protected val tag: String = this.javaClass.simpleName

    private val _appError = MutableStateFlow<AppError?>(null)
    /**
     * Exposes the current error state to the UI layer.
     */
    val appError: StateFlow<AppError?> = _appError.asStateFlow()

    private val _successMessage = Channel<String>(Channel.BUFFERED)
    /**
     * Exposes success messages to the UI layer, typically shown as Toasts.
     */
    val successMessage: Flow<String> = _successMessage.receiveAsFlow()

    /**
     * Handles an exception by mapping it to an [AppError] and updating the error state.
     * It also logs the error for debugging purposes.
     *
     * @param throwable The exception to handle.
     */
    fun handleError(throwable: Throwable) {
        AppLogger.e(tag, "Error encountered: ${throwable.message}", throwable)
        
        val error = when (throwable) {
            is SocketTimeoutException -> AppError.TimeoutError
            is UnknownHostException, is ConnectException -> AppError.NoInternetError
            is IOException -> AppError.NetworkError
            is IllegalStateException -> {
                // Specific check for common business logic / Firebase related illegal states
                if (throwable.message?.contains("already exists", ignoreCase = true) == true) {
                    AppError.ValidationError(throwable.message ?: "Record already exists")
                } else {
                    AppError.UnknownError(throwable.message ?: "An unexpected state occurred")
                }
            }
            is SecurityException -> AppError.PermissionError(throwable.message ?: "Permission denied")
            else -> AppError.UnknownError(throwable.message ?: "An unknown error occurred")
        }
        
        _appError.value = error
    }

    /**
     * Triggers a success message to be shown on the UI.
     *
     * @param message The message to display.
     */
    fun showSuccess(message: String) {
        _successMessage.trySend(message)
    }

    /**
     * Resets the error state to null. Should be called after the error has been consumed or dismissed.
     */
    fun clearError() {
        _appError.value = null
    }
}
