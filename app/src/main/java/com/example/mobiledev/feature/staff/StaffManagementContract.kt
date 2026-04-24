package com.example.mobiledev.feature.staff

import com.example.mobiledev.data.model.StaffInvitation
import com.example.mobiledev.data.model.StaffMember
import com.example.mobiledev.data.model.StaffRole
import com.example.mobiledev.data.model.StaffStatus

data class StaffManagementState(
    val staffList: List<StaffMember> = emptyList(),
    val invitations: List<StaffInvitation> = emptyList(),
    val isLoading: Boolean = false,
    val isInviteDialogOpen: Boolean = false,
    val staffToRemove: StaffMember? = null,
    val invitationToCancel: StaffInvitation? = null,
    val error: String? = null
)

sealed class StaffManagementEvent {
    data object LoadData : StaffManagementEvent()
    data class InviteStaff(val email: String, val role: StaffRole) : StaffManagementEvent()
    data class UpdateStaff(val id: String, val role: StaffRole?, val status: StaffStatus?) : StaffManagementEvent()
    data class RemoveStaff(val id: String) : StaffManagementEvent()
    data class ResendInvitation(val id: String) : StaffManagementEvent()
    data class CancelInvitation(val id: String) : StaffManagementEvent()
    data class ToggleInviteDialog(val isOpen: Boolean) : StaffManagementEvent()
    data class ShowRemoveStaffConfirmation(val staff: StaffMember?) : StaffManagementEvent()
    data class ShowCancelInvitationConfirmation(val invitation: StaffInvitation?) : StaffManagementEvent()
}

