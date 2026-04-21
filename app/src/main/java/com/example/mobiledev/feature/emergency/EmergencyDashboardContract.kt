package com.example.mobiledev.feature.emergency

import com.example.mobiledev.data.model.Ambulance
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus

data class EmergencyDashboardState(
    val requests: List<EmergencyRequest> = emptyList(),
    val filteredRequests: List<EmergencyRequest> = emptyList(),
    val ambulances: List<Ambulance> = emptyList(),
    val selectedRequest: EmergencyRequest? = null,
    val selectedRequestForAssignment: EmergencyRequest? = null,
    val statusFilter: EmergencyStatus? = null,
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val hasMore: Boolean = true,
    val totalRequestsToday: Int = 0,
    val pendingRequestsCount: Int = 0,
    val averageResponseTimeMinutes: Double = 0.0,
    val newRequestsCount: Int = 0
)

sealed class EmergencyDashboardEvent {
    data object LoadData : EmergencyDashboardEvent()
    data object RefreshData : EmergencyDashboardEvent()
    data object LoadNextPage : EmergencyDashboardEvent()
    data class UpdateRequestStatus(val requestId: String, val status: EmergencyStatus) : EmergencyDashboardEvent()
    data class OpenRequestDetails(val request: EmergencyRequest) : EmergencyDashboardEvent()
    data object CloseRequestDetails : EmergencyDashboardEvent()
    data class OpenAssignmentDialog(val request: EmergencyRequest) : EmergencyDashboardEvent()
    data object CloseAssignmentDialog : EmergencyDashboardEvent()
    data class AssignAmbulance(val requestId: String, val ambulanceId: String) : EmergencyDashboardEvent()
    data class FilterByStatus(val status: EmergencyStatus?) : EmergencyDashboardEvent()
    data class FilterByDateRange(val start: Long?, val end: Long?) : EmergencyDashboardEvent()
    data object ClearNewRequestBadge : EmergencyDashboardEvent()
}

