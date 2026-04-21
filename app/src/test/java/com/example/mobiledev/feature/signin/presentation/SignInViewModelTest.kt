package com.example.mobiledev.feature.signin.presentation

import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.data.security.AuthSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SignInViewModelTest {

    private val viewModel = SignInViewModel(FakeUserRepository(isAuthenticated = true), AuthSessionManager())

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.emailOrPhone)
        assertEquals("", state.password)
        assertNull(state.errorMessage)
    }

    @Test
    fun `submitSignIn with blank fields sets missing email or phone error`() {
        viewModel.submitSignIn()

        assertEquals("Email or phone number is required.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitSignIn with invalid email or phone sets format error`() {
        viewModel.onEvent(SignInEvent.EmailOrPhoneChanged("not-valid"))
        viewModel.onEvent(SignInEvent.PasswordChanged("Secret123"))

        viewModel.submitSignIn()

        assertEquals("Enter a valid email or phone number.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitSignIn with valid email and password sets success message`() {
        viewModel.onEvent(SignInEvent.EmailOrPhoneChanged("user@example.com"))
        viewModel.onEvent(SignInEvent.PasswordChanged("Secret123"))

        viewModel.submitSignIn()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }

    private class FakeUserRepository(
        private val isAuthenticated: Boolean
    ) : UserRepository {
        override suspend fun getUsers(): List<User> = emptyList()

        override suspend fun addUser(
            name: String,
            email: String,
            phone: String,
            password: String,
            role: String,
            hospitalId: String?,
            accountStatus: String
        ): User =
            User(id = "1", name = name, email = email, phone = phone, password = password, role = role, hospitalId = hospitalId, accountStatus = accountStatus)

        override suspend fun authenticateUser(emailOrPhone: String, password: String): User? =
            if (isAuthenticated) User(id = "1", name = "Test", email = emailOrPhone, phone = "", password = password) else null

        override suspend fun authenticateHospital(email: String, password: String): User? = null

        override suspend fun removeUser(userId: String) = Unit
    }
}

