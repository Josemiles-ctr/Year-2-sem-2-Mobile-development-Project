package com.example.mobiledev.feature.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.core.base.BaseViewModel
import com.example.mobiledev.data.model.StaffRole
import com.example.mobiledev.data.model.StaffStatus
import com.example.mobiledev.data.repository.StaffRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StaffViewModel(private val repository: StaffRepository) : BaseViewModel() {

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
            is StaffManagementEvent.ShowRemoveStaffConfirmation -> {
                _uiState.update { it.copy(staffToRemove = event.staff) }
            }
            is StaffManagementEvent.ShowCancelInvitationConfirmation -> {
                _uiState.update { it.copy(invitationToCancel = event.invitation) }
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
                        isLoading = false
                    )
                }
            }.catch { e ->
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }.collect()
        }
    }

    private fun inviteStaff(email: String, role: StaffRole) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.inviteStaff(email, role).onSuccess {
                _uiState.update { it.copy(isInviteDialogOpen = false) }
                showSuccess("Staff invited successfully")
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateStaff(id: String, role: StaffRole?, status: StaffStatus?) {
        viewModelScope.launch {
            repository.updateStaff(id, role, status).onSuccess {
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                handleError(e)
            }
        }
    }

    private fun removeStaff(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(staffToRemove = null, isLoading = true) }
            repository.removeStaff(id).onSuccess {
                showSuccess("Staff member removed")
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun resendInvitation(id: String) {
        viewModelScope.launch {
            repository.resendInvitation(id).onSuccess {
                // Show success message maybe
            }.onFailure { e ->
                handleError(e)
            }
        }
    }

    private fun cancelInvitation(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(invitationToCancel = null, isLoading = true) }
            repository.cancelInvitation(id).onSuccess {
                showSuccess("Invitation cancelled")
                onEvent(StaffManagementEvent.LoadData)
            }.onFailure { e ->
                handleError(e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
