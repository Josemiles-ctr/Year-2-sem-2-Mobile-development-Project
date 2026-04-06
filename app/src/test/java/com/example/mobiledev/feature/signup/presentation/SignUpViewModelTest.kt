package com.example.mobiledev.feature.signup.presentation

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setUp() {
        viewModel = SignUpViewModel()
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial users list is empty`() {
        assertTrue(viewModel.users.value.isEmpty())
    }

    @Test
    fun `initial error message is null`() {
        assertNull(viewModel.errorMessage.value)
    }

    // ── addUser — happy path ──────────────────────────────────────────────────

    @Test
    fun `addUser with valid inputs adds user to list`() {
        viewModel.addUser("Alice", "alice@example.com")
        assertEquals(1, viewModel.users.value.size)
        assertEquals("Alice", viewModel.users.value.first().name)
        assertEquals("alice@example.com", viewModel.users.value.first().email)
        assertEquals("", viewModel.users.value.first().phone)
    }

    @Test
    fun `addUser assigns auto-incrementing ids`() {
        viewModel.addUser("Alice", "alice@example.com")
        viewModel.addUser("Bob", "bob@example.com")
        val ids = viewModel.users.value.map { it.id }
        assertEquals(listOf(1, 2), ids)
    }

    @Test
    fun `addUser trims whitespace from name and email`() {
        viewModel.addUser("  Alice  ", "  alice@example.com  ")
        val user = viewModel.users.value.first()
        assertEquals("Alice", user.name)
        assertEquals("alice@example.com", user.email)
    }

    @Test
    fun `addUser with valid data clears any previous error`() {
        viewModel.addUser("A", "bad") // trigger an error first
        viewModel.addUser("Alice", "alice@example.com")
        assertNull(viewModel.errorMessage.value)
    }

    // ── addUser — validation errors ───────────────────────────────────────────

    @Test
    fun `addUser with invalid name does not add user`() {
        viewModel.addUser("A", "alice@example.com")
        assertTrue(viewModel.users.value.isEmpty())
    }

    @Test
    fun `addUser with invalid name sets error message`() {
        viewModel.addUser("A", "alice@example.com")
        assertNotNull(viewModel.errorMessage.value)
    }

    @Test
    fun `addUser with invalid email does not add user`() {
        viewModel.addUser("Alice", "not-an-email")
        assertTrue(viewModel.users.value.isEmpty())
    }

    @Test
    fun `addUser with invalid email sets error message`() {
        viewModel.addUser("Alice", "not-an-email")
        assertNotNull(viewModel.errorMessage.value)
    }

    // ── removeUser ────────────────────────────────────────────────────────────

    @Test
    fun `removeUser removes the correct user by id`() {
        viewModel.addUser("Alice", "alice@example.com")
        viewModel.addUser("Bob", "bob@example.com")
        val aliceId = viewModel.users.value.first { it.name == "Alice" }.id
        viewModel.removeUser(aliceId)
        assertEquals(1, viewModel.users.value.size)
        assertEquals("Bob", viewModel.users.value.first().name)
    }

    @Test
    fun `removeUser with non-existent id leaves list unchanged`() {
        viewModel.addUser("Alice", "alice@example.com")
        viewModel.removeUser(999)
        assertEquals(1, viewModel.users.value.size)
    }

    @Test
    fun `removeUser all users results in empty list`() {
        viewModel.addUser("Alice", "alice@example.com")
        val id = viewModel.users.value.first().id
        viewModel.removeUser(id)
        assertTrue(viewModel.users.value.isEmpty())
    }

    // -- submitSignUp ----------------------------------------------------------

    @Test
    fun `submitSignUp with valid data adds user and resets form`() {
        viewModel.onFullNameChange("Alice")
        viewModel.onPhoneNumberChange("0712345678")
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPasswordChange("Secure123")
        viewModel.onConfirmPasswordChange("Secure123")

        viewModel.submitSignUp()

        val user = viewModel.users.value.first()
        assertEquals("Alice", user.name)
        assertEquals("0712345678", user.phone)
        assertNull(viewModel.signUpUiState.value.errorMessage)
        assertEquals("Account created successfully.", viewModel.signUpUiState.value.successMessage)
    }

    @Test
    fun `submitSignUp with mismatched passwords sets error`() {
        viewModel.onFullNameChange("Alice")
        viewModel.onPhoneNumberChange("0712345678")
        viewModel.onEmailChange("alice@example.com")
        viewModel.onPasswordChange("Secure123")
        viewModel.onConfirmPasswordChange("Wrong123")

        viewModel.submitSignUp()

        assertTrue(viewModel.users.value.isEmpty())
        assertEquals("Passwords do not match.", viewModel.signUpUiState.value.errorMessage)
    }
}

