package com.example.mobiledev.feature.staff

import com.example.mobiledev.data.model.*

data class StaffManagementState(
    val staffList: List<StaffMember> = emptyList(),
    val invitations: List<StaffInvitation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isInviteDialogOpen: Boolean = false
)

sealed class StaffManagementEvent {
    object LoadData : StaffManagementEvent()
    data class InviteStaff(val email: String, val role: StaffRole) : StaffManagementEvent()
    data class UpdateStaff(val id: String, val role: StaffRole?, val status: StaffStatus?) : StaffManagementEvent()
    data class RemoveStaff(val id: String) : StaffManagementEvent()
    data class ResendInvitation(val id: String) : StaffManagementEvent()
    data class CancelInvitation(val id: String) : StaffManagementEvent()
    data class ToggleInviteDialog(val isOpen: Boolean) : StaffManagementEvent()
}
