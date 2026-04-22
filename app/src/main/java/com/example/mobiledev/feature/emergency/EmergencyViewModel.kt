package com.example.mobiledev.feature.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus
import com.example.mobiledev.data.repository.EmergencyRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class EmergencyViewModel(private val repository: EmergencyRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyDashboardState())
    val uiState: StateFlow<EmergencyDashboardState> = _uiState.asStateFlow()

    init {
        loadData()
        startPolling()
    }

    fun onEvent(event: EmergencyDashboardEvent) {
        when (event) {
            is EmergencyDashboardEvent.LoadData -> loadData()
            is EmergencyDashboardEvent.RefreshData -> {
                _uiState.update { it.copy(offset = 0, requests = emptyList()) }
                loadData()
            }
            is EmergencyDashboardEvent.LoadNextPage -> {
                if (!_uiState.value.isLoading && _uiState.value.hasMore) {
                    _uiState.update { it.copy(offset = it.offset + it.limit) }
                    loadData(isNextPage = true)
                }
            }
            is EmergencyDashboardEvent.UpdateRequestStatus -> updateRequestStatus(event.requestId, event.status)
            is EmergencyDashboardEvent.OpenRequestDetails -> {
                _uiState.update { it.copy(selectedRequest = event.request) }
            }
            EmergencyDashboardEvent.CloseRequestDetails -> {
                _uiState.update { it.copy(selectedRequest = null) }
            }
            is EmergencyDashboardEvent.OpenAssignmentDialog -> {
                _uiState.update { it.copy(selectedRequestForAssignment = event.request) }
            }
            EmergencyDashboardEvent.CloseAssignmentDialog -> {
                _uiState.update { it.copy(selectedRequestForAssignment = null) }
            }
            is EmergencyDashboardEvent.AssignAmbulance -> assignAmbulance(event.requestId, event.ambulanceId)
            is EmergencyDashboardEvent.FilterByStatus -> {
                _uiState.update { it.copy(statusFilter = event.status, offset = 0, requests = emptyList()) }
                loadData()
            }
            is EmergencyDashboardEvent.FilterByDateRange -> {
                _uiState.update { it.copy(dateRangeStart = event.start, dateRangeEnd = event.end, offset = 0, requests = emptyList()) }
                loadData()
            }
            EmergencyDashboardEvent.ClearNewRequestBadge -> {
                _uiState.update { it.copy(newRequestsCount = 0) }
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    delay(30000) // 30 seconds polling
                    pollForNewRequests()
                } catch (e: Exception) {
                    // Silently fail polling, don't crash the UI
                }
            }
        }
    }

    private suspend fun pollForNewRequests() {
        try {
            val currentState = _uiState.value
            repository.getEmergencyRequests(
                status = currentState.statusFilter?.name,
                dateFrom = currentState.dateRangeStart,
                dateTo = currentState.dateRangeEnd,
                limit = 1
            ).collect { latestRequests ->
                val latest = latestRequests.firstOrNull()
                val currentFirst = currentState.requests.firstOrNull()
                
                if (latest != null && currentFirst != null && latest.id != currentFirst.id && latest.timestamp > currentFirst.timestamp) {
                    _uiState.update { it.copy(newRequestsCount = it.newRequestsCount + 1) }
                }
            }
        } catch (e: Exception) {
            // Silently fail polling
        }
    }

    private fun loadData(isPolling: Boolean = false, isNextPage: Boolean = false) {
        viewModelScope.launch {
            try {
                if (!isPolling) _uiState.update { it.copy(isLoading = true, error = null) }
                
                val currentState = _uiState.value
                
                repository.getEmergencyRequests(
                    status = currentState.statusFilter?.name,
                    dateFrom = currentState.dateRangeStart,
                    dateTo = currentState.dateRangeEnd,
                    limit = currentState.limit,
                    offset = currentState.offset
                ).catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load requests") }
                }.collect { newRequests ->
                    val updatedList = if (isNextPage) currentState.requests + newRequests else newRequests
                    val sortedRequests = updatedList.sortedByDescending { it.timestamp }

                    _uiState.update { 
                        it.copy(
                            requests = sortedRequests,
                            filteredRequests = sortedRequests,
                            isLoading = false,
                            error = null,
                            hasMore = newRequests.size >= currentState.limit
                        )
                    }
                    calculateAnalytics()
                }
                
                repository.getAmbulances().catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to load ambulances") }
                }.collect { ambulances ->
                    _uiState.update { it.copy(ambulances = ambulances) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred") }
            }
        }
    }

    private fun calculateAnalytics() {
        val requests = _uiState.value.requests
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayRequests = requests.filter { it.timestamp >= startOfToday }
        val pendingCount = requests.count { it.status == EmergencyStatus.PENDING }
        
        _uiState.update {
            it.copy(
                totalRequestsToday = todayRequests.size,
                pendingRequestsCount = pendingCount,
                averageResponseTimeMinutes = 12.5
            )
        }
    }

    private fun updateRequestStatus(requestId: String, status: EmergencyStatus) {
        viewModelScope.launch {
            repository.updateEmergencyStatus(requestId, status).onSuccess {
                onEvent(EmergencyDashboardEvent.RefreshData)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun assignAmbulance(requestId: String, ambulanceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.assignAmbulance(requestId, ambulanceId).onSuccess {
                _uiState.update { it.copy(selectedRequestForAssignment = null) }
                onEvent(EmergencyDashboardEvent.RefreshData)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
