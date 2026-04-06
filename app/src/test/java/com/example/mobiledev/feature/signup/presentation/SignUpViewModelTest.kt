package com.example.mobiledev.feature.signup.presentation

import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.mobiledev.test.MainDispatcherRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SignUpViewModel

    @Before
    fun setUp() {
        viewModel = SignUpViewModel(FakeUserRepository())
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addUser with valid inputs adds user to list`() {
        runTest {
            viewModel.addUser("Alice", "alice@example.com")
            advanceUntilIdle()
            assertEquals(1, viewModel.users.value.size)
            assertEquals("Alice", viewModel.users.value.first().name)
            assertEquals("alice@example.com", viewModel.users.value.first().email)
            assertEquals("", viewModel.users.value.first().phone)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addUser assigns auto-incrementing ids`() {
        runTest {
            viewModel.addUser("Alice", "alice@example.com")
            viewModel.addUser("Bob", "bob@example.com")
            advanceUntilIdle()
            val ids = viewModel.users.value.map { it.id }
            assertEquals(listOf("1", "2"), ids)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addUser trims whitespace from name and email`() {
        runTest {
            viewModel.addUser("  Alice  ", "  alice@example.com  ")
            advanceUntilIdle()
            val user = viewModel.users.value.first()
            assertEquals("Alice", user.name)
            assertEquals("alice@example.com", user.email)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addUser with valid data clears any previous error`() {
        runTest {
            viewModel.addUser("A", "bad") // trigger an error first
            viewModel.addUser("Alice", "alice@example.com")
            advanceUntilIdle()
            assertNull(viewModel.errorMessage.value)
        }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `removeUser removes the correct user by id`() {
        runTest {
            viewModel.addUser("Alice", "alice@example.com")
            viewModel.addUser("Bob", "bob@example.com")
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
            viewModel.addUser("Alice", "alice@example.com")
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
            viewModel.addUser("Alice", "alice@example.com")
            advanceUntilIdle()
            val id = viewModel.users.value.first().id
            viewModel.removeUser(id)
            advanceUntilIdle()
            assertTrue(viewModel.users.value.isEmpty())
        }
    }

    // -- submitSignUp ----------------------------------------------------------

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

    private class FakeUserRepository : UserRepository {
        private val users = mutableListOf<User>()

        override suspend fun getUsers(): List<User> = users.toList()

        override suspend fun addUser(name: String, email: String, phone: String, password: String): User {
            val user = User(
                id = (users.size + 1).toString(),
                name = name,
                email = email,
                phone = phone,
                password = password
            )
            users += user
            return user
        }

        override suspend fun authenticateUser(emailOrPhone: String, password: String): Boolean =
            users.any { user ->
                (user.email.equals(emailOrPhone, ignoreCase = true) || user.phone == emailOrPhone) &&
                    user.password == password
            }

        override suspend fun removeUser(userId: String) {
            users.removeAll { it.id == userId }
        }
    }
}

