package com.example.mobiledev.feature.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.model.StaffRole
import com.example.mobiledev.data.model.StaffStatus
import com.example.mobiledev.data.repository.StaffRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StaffViewModel(private val repository: StaffRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementState())
    val uiState: StateFlow<StaffManagementState> = _uiState.asStateFlow()

    init {
        onEvent(StaffManagementEvent.LoadData)
    }

    fun onEvent(event: StaffManagementEvent) {
        when (event) {
            is StaffManagementEvent.LoadData -> loadData()
            is StaffManagementEvent.InviteStaff -> inviteStaff(event.email, event.role)
            is StaffManagementEvent.UpdateStaff -> updateStaff(event.id, event.role, event.status)
            is StaffManagementEvent.RemoveStaff -> removeStaff(event.id)
            is StaffManagementEvent.ResendInvitation -> resendInvitation(event.id)
            is StaffManagementEvent.CancelInvitation -> cancelInvitation(event.id)
            is StaffManagementEvent.ToggleInviteDialog -> {
                _uiState.update { it.copy(isInviteDialogOpen = event.isOpen) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            combine(
                repository.getStaffList(),
                repository.getPendingInvitations()
            ) { staff, invitations ->
                _uiState.update { 
                    it.copy(
                        staffList = staff,
                        invitations = invitations,
                        isLoading = false,
                        error = null
                    )
                }
            }.catch { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }.collect()
        }
    }

    private fun inviteStaff(email: String, role: StaffRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.inviteStaff(email, role).onSuccess {
                _uiState.update { it.copy(isInviteDialogOpen = false) }
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun updateStaff(id: String, role: StaffRole?, status: StaffStatus?) {
        viewModelScope.launch {
            repository.updateStaff(id, role, status).onSuccess {
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun removeStaff(id: String) {
        viewModelScope.launch {
            repository.removeStaff(id).onSuccess {
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun resendInvitation(id: String) {
        viewModelScope.launch {
            repository.resendInvitation(id).onSuccess {
                // Show success message maybe
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun cancelInvitation(id: String) {
        viewModelScope.launch {
            repository.cancelInvitation(id).onSuccess {
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
