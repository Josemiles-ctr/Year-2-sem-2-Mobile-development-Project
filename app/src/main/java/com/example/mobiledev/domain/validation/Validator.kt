package com.example.mobiledev.domain.validation

object Validator {

    // Pure-Kotlin regex — safe for JVM unit tests (no Android framework needed)
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    private val NON_DIGIT_REGEX = Regex("[^0-9]")

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && EMAIL_REGEX.matches(email.trim())

    fun isValidName(name: String): Boolean =
        name.trim().length >= 2

    fun isValidPhone(phone: String): Boolean {
        val normalized = phone.replace(NON_DIGIT_REGEX, "")
        return normalized.length in 10..15
    }

    fun isValidPassword(password: String): Boolean =
        PASSWORD_REGEX.matches(password)

    fun passwordsMatch(password: String, confirmPassword: String): Boolean =
        password == confirmPassword
}

