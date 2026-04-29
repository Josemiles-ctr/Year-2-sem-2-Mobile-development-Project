package com.example.mobiledev.core.error

import android.content.Context
import com.example.mobiledev.R

/**
 * Maps [AppError] to a user-friendly message string.
 */
fun AppError.toUserMessage(context: Context): String {
    return when (this) {
        is AppError.NetworkError -> context.getString(R.string.error_network)
        is AppError.TimeoutError -> context.getString(R.string.error_timeout)
        is AppError.NoInternetError -> context.getString(R.string.error_no_internet)
        is AppError.ValidationError -> message
        is AppError.PermissionError -> message
        is AppError.ApiError -> message
        is AppError.UnknownError -> message.ifBlank { context.getString(R.string.error_unknown) }
    }
}
