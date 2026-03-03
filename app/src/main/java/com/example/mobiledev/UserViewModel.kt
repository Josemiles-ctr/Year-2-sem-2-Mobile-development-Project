package com.example.mobiledev

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addUser(name: String, email: String) {
        if (!Validator.isValidName(name)) {
            _errorMessage.value = "Name must be at least 2 characters."
            return
        }
        if (!Validator.isValidEmail(email)) {
            _errorMessage.value = "Invalid email address."
            return
        }
        val nextId = (_users.value.maxOfOrNull { it.id } ?: 0) + 1
        _users.value = _users.value + User(id = nextId, name = name.trim(), email = email.trim())
        _errorMessage.value = null
    }

    fun removeUser(userId: Int) {
        _users.value = _users.value.filter { it.id != userId }
    }
}
