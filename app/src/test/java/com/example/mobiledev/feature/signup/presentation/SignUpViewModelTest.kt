package com.example.mobiledev.feature.signup.presentation

import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.UserRepository
import com.example.mobiledev.data.security.AuthSessionManager
import com.example.mobiledev.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setUp() {
        viewModel = SignUpViewModel(FakeUserRepository(), AuthSessionManager())
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

    // ── removeUser ────────────────────────────────────────────────────────────

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeUser removes the correct user by id`() {
        runTest {
            submitValidSignUp(name = "Alice", email = "alice@example.com", phone = "0712345678")
            submitValidSignUp(name = "Bob", email = "bob@example.com", phone = "0722222222")
            advanceUntilIdle()
            val aliceId = viewModel.users.value.first { it.name == "Alice" }.id
            viewModel.removeUser(aliceId)
            advanceUntilIdle()
            assertEquals(1, viewModel.users.value.size)
            assertEquals("Bob", viewModel.users.value.first().name)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeUser with non-existent id leaves list unchanged`() {
        runTest {
            submitValidSignUp(name = "Alice", email = "alice@example.com", phone = "0712345678")
            advanceUntilIdle()
            viewModel.removeUser("999")
            advanceUntilIdle()
            assertEquals(1, viewModel.users.value.size)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeUser all users results in empty list`() {
        runTest {
            submitValidSignUp(name = "Alice", email = "alice@example.com", phone = "0712345678")
            advanceUntilIdle()
            val id = viewModel.users.value.first().id
            viewModel.removeUser(id)
            advanceUntilIdle()
            assertTrue(viewModel.users.value.isEmpty())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submitSignUp with valid data adds user and resets form`() {
        runTest {
            viewModel.onFullNameChange("Alice")
            viewModel.onPhoneNumberChange("0712345678")
            viewModel.onEmailChange("alice@example.com")
            viewModel.onPasswordChange("Secure123")
            viewModel.onConfirmPasswordChange("Secure123")

            viewModel.submitSignUp()
            advanceUntilIdle()

            val user = viewModel.users.value.first()
            assertEquals("Alice", user.name)
            assertEquals("0712345678", user.phone)
            assertNull(viewModel.signUpUiState.value.errorMessage)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `submitSignUp emits navigate to dashboard on success`() = runTest {
        submitValidSignUp()
        advanceUntilIdle()

        val event = viewModel.navigationEvents.first()
        assertEquals(SignUpViewModel.NavigationEvent.NavigateToDashboard, event)
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

    private fun submitValidSignUp(
        name: String = "Alice",
        phone: String = "0712345678",
        email: String = "alice@example.com",
        password: String = "Secure123"
    ) {
        viewModel.onFullNameChange(name)
        viewModel.onPhoneNumberChange(phone)
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        viewModel.onConfirmPasswordChange(password)
        viewModel.submitSignUp()
    }

    private class FakeUserRepository : UserRepository {
        private val users = mutableListOf<User>()

        override suspend fun getUsers(): List<User> = users.toList()

        override suspend fun addUser(
            name: String,
            email: String,
            phone: String,
            password: String,
            role: String,
            hospitalId: String?,
            accountStatus: String
        ): User {
            val user = User(
                id = (users.size + 1).toString(),
                name = name,
                email = email,
                phone = phone,
                password = password,
                role = role,
                hospitalId = hospitalId,
                accountStatus = accountStatus
            )
            users += user
            return user
        }

        override suspend fun authenticateUser(emailOrPhone: String, password: String): User? =
            users.firstOrNull { user ->
                (user.email.equals(emailOrPhone, ignoreCase = true) || user.phone == emailOrPhone) &&
                    user.password == password
            }

        override suspend fun authenticateHospital(email: String, password: String): User? = null

        override suspend fun removeUser(userId: String) {
            users.removeAll { it.id == userId }
        }
    }
}

