package com.example.mobiledev.core.error

/**
 * Represents a centralized error handling system for the application.
 * This sealed class categorizes various types of errors that can occur during
 * the application's lifecycle, allowing for consistent error handling and
 * user-friendly feedback across all ViewModels and UI layers.
 */
sealed class AppError {
    /**
     * Represents a general network-related error (e.g., DNS failure, server down).
     */
    data object NetworkError : AppError()

    /**
     * Represents a timeout error when a request takes too long to complete.
     */
    data object TimeoutError : AppError()

    /**
     * Represents a specific error indicating no active internet connection.
     */
    data object NoInternetError : AppError()

    /**
     * Represents an error during data validation (e.g., invalid email format).
     * @property message A user-friendly validation error message.
     */
    data class ValidationError(val message: String) : AppError()

    /**
     * Represents an error due to insufficient permissions (e.g., denied location access).
     * @property message Information about the denied permission or context.
     */
    data class PermissionError(val message: String) : AppError()

    /**
     * Represents a server-side API error with a specific status code and message.
     * @property code The HTTP status code or a custom API error code.
     * @property message The error message returned by the API.
     */
    data class ApiError(val code: Int, val message: String) : AppError()

    /**
     * Represents an unexpected or unknown error that does not fit into other categories.
     * @property message A brief description of the error or its origin.
     */
    data class UnknownError(val message: String) : AppError()
}
