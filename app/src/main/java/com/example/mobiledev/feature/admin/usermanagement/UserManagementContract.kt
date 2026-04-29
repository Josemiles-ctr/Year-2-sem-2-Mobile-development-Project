package com.example.mobiledev.feature.admin.usermanagement

import com.example.mobiledev.data.model.User

data class UserManagementState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val userToEdit: User? = null,
    val userToDelete: User? = null
)

sealed class UserManagementEvent {
    data object LoadUsers : UserManagementEvent()
    data class SearchQueryChanged(val query: String) : UserManagementEvent()
    data class EditUser(val user: User?) : UserManagementEvent()
    data class UpdateUser(val user: User) : UserManagementEvent()
    data class ConfirmDeleteUser(val user: User?) : UserManagementEvent()
    data object DeleteUser : UserManagementEvent()
    data class ToggleUserStatus(val user: User) : UserManagementEvent()
}
