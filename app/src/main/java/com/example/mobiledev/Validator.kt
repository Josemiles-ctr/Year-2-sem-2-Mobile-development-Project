package com.example.mobiledev

object Validator {

    // Pure-Kotlin regex — safe for JVM unit tests (no Android framework needed)
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_REGEX.matches(email.trim())

    fun isValidName(name: String): Boolean =
        name.trim().length >= 2
}
