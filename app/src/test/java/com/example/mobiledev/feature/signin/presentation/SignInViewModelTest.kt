package com.example.mobiledev.feature.signin.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SignInViewModelTest {

    private val viewModel = SignInViewModel()

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertEquals("", state.emailOrPhone)
        assertEquals("", state.password)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
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
        assertEquals("Login request submitted.", state.successMessage)
    }
}

