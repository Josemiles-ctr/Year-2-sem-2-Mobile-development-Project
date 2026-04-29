package com.example.mobiledev.feature.admin.usermanagement

import androidx.lifecycle.viewModelScope
import com.example.mobiledev.core.base.BaseViewModel
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(UserManagementState())
    val uiState: StateFlow<UserManagementState> = _uiState.asStateFlow()

    init {
        onEvent(UserManagementEvent.LoadUsers)
    }

    fun onEvent(event: UserManagementEvent) {
        when (event) {
            UserManagementEvent.LoadUsers -> loadUsers()
            is UserManagementEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is UserManagementEvent.EditUser -> {
                _uiState.update { it.copy(userToEdit = event.user) }
            }
            is UserManagementEvent.UpdateUser -> updateUser(event.user)
            is UserManagementEvent.ConfirmDeleteUser -> {
                _uiState.update { it.copy(userToDelete = event.user) }
            }
            UserManagementEvent.DeleteUser -> deleteUser()
            is UserManagementEvent.ToggleUserStatus -> toggleUserStatus(event.user)
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val users = userRepository.getUsers()
                _uiState.update { it.copy(users = users, isLoading = false) }
            } catch (e: Exception) {
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateUser(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userToEdit = null) }
            userRepository.updateUser(user).onSuccess {
                loadUsers()
                showSuccess("User updated successfully")
            }.onFailure { e ->
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun deleteUser() {
        val user = _uiState.value.userToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userToDelete = null) }
            try {
                userRepository.removeUser(user.id)
                loadUsers()
                showSuccess("User deleted successfully")
            } catch (e: Exception) {
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun toggleUserStatus(user: User) {
        val newStatus = if (user.accountStatus == "ACTIVE") "INACTIVE" else "ACTIVE"
        val updatedUser = user.copy(accountStatus = newStatus)
        updateUser(updatedUser)
    }
}
