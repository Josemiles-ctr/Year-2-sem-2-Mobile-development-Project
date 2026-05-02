package com.example.mobiledev.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.local.entity.NotificationEntity
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.data.security.AuthSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true
)

class NotificationsViewModel(
    private val repository: ResQRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val userId = authSessionManager.currentPrincipal.userId ?: return
        viewModelScope.launch {
            repository.getNotificationsForUserStream(userId).collectLatest { notifications ->
                _uiState.update { it.copy(notifications = notifications, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.getUnreadNotificationCountStream(userId).collectLatest { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }
}

class NotificationsViewModelFactory(
    private val repository: ResQRepository,
    private val authSessionManager: AuthSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(repository, authSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
