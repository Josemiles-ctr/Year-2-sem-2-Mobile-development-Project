package com.example.mobiledev.feature.emergency

import com.example.mobiledev.data.model.*

data class EmergencyDashboardState(
    val requests: List<EmergencyRequest> = emptyList(),
    val filteredRequests: List<EmergencyRequest> = emptyList(),
    val ambulances: List<Ambulance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedRequest: EmergencyRequest? = null,
    val selectedRequestForAssignment: EmergencyRequest? = null,
    
    // Filtering
    val statusFilter: EmergencyStatus? = null,
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    
    // Analytics
    val totalRequestsToday: Int = 0,
    val pendingRequestsCount: Int = 0,
    val averageResponseTimeMinutes: Double = 0.0,
    
    // UI state
    val lastRefreshTime: Long = 0L,
    val newRequestsCount: Int = 0,
    
    // Pagination
    val limit: Int = 20,
    val offset: Int = 0,
    val hasMore: Boolean = true
)

sealed class EmergencyDashboardEvent {
    object LoadData : EmergencyDashboardEvent()
    object RefreshData : EmergencyDashboardEvent()
    object LoadNextPage : EmergencyDashboardEvent()
    data class UpdateRequestStatus(val requestId: String, val status: EmergencyStatus) : EmergencyDashboardEvent()
    data class OpenRequestDetails(val request: EmergencyRequest) : EmergencyDashboardEvent()
    object CloseRequestDetails : EmergencyDashboardEvent()
    data class OpenAssignmentDialog(val request: EmergencyRequest) : EmergencyDashboardEvent()
    object CloseAssignmentDialog : EmergencyDashboardEvent()
    data class AssignAmbulance(val requestId: String, val ambulanceId: String) : EmergencyDashboardEvent()
    
    data class FilterByStatus(val status: EmergencyStatus?) : EmergencyDashboardEvent()
    data class FilterByDateRange(val start: Long?, val end: Long?) : EmergencyDashboardEvent()
    
    object ClearNewRequestBadge : EmergencyDashboardEvent()
}
