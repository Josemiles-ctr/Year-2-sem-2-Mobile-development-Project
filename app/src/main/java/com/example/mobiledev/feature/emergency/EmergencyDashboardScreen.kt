package com.example.mobiledev.feature.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.data.model.*
import com.example.mobiledev.ui.components.AppLoadingIndicator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun EmergencyDashboardScreen(
    viewModel: EmergencyViewModel,
    onAmbulanceClick: (String) -> Unit = {}
) {
    val state = viewModel.uiState.collectAsState().value
    EmergencyDashboardContent(
        state = state,
        onEvent = viewModel::onEvent,
        onAmbulanceClick = onAmbulanceClick
    )
}

@Composable
fun EmergencyDashboardContent(
    state: EmergencyDashboardState,
    onEvent: (EmergencyDashboardEvent) -> Unit,
    onAmbulanceClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Emergency Dispatch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Monitor and manage live emergency requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AnalyticsSummary(state)
            
            Spacer(modifier = Modifier.height(24.dp))

            FilterSection(
                selectedStatus = state.statusFilter,
                onStatusSelected = { onEvent(EmergencyDashboardEvent.FilterByStatus(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading && state.requests.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredRequests) { request ->
                        EmergencyRequestItem(
                            request = request,
                            onClick = { onEvent(EmergencyDashboardEvent.OpenRequestDetails(request)) },
                            onAssignClick = { onEvent(EmergencyDashboardEvent.OpenAssignmentDialog(request)) }
                        )
                    }
                    
                    if (state.filteredRequests.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                                Text(
                                    "No emergency requests found.",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        state.selectedRequest?.let { request ->
            RequestDetailsDialog(
                request = request,
                availableAmbulancesCount = state.ambulances.count { it.status == AmbulanceStatus.AVAILABLE },
                onDismiss = { onEvent(EmergencyDashboardEvent.CloseRequestDetails) },
                onAssignClick = {
                    onEvent(EmergencyDashboardEvent.CloseRequestDetails)
                    onEvent(EmergencyDashboardEvent.OpenAssignmentDialog(request))
                }
            )
        }

        state.selectedRequestForAssignment?.let { request ->
            AssignAmbulanceDialog(
                ambulances = state.ambulances.filter { it.status == AmbulanceStatus.AVAILABLE },
                onDismiss = { onEvent(EmergencyDashboardEvent.CloseAssignmentDialog) },
                onAssign = { ambulanceId ->
                    onEvent(EmergencyDashboardEvent.AssignAmbulance(request.id, ambulanceId))
                },
                onAmbulanceClick = onAmbulanceClick
            )
        }
    }
}

@Composable
fun AnalyticsSummary(state: EmergencyDashboardState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnalyticsItem(
            label = "Today",
            value = state.totalRequestsToday.toString(),
            icon = Icons.Default.Assessment,
            modifier = Modifier.weight(1f)
        )
        AnalyticsItem(
            label = "Pending",
            value = state.pendingRequestsCount.toString(),
            icon = Icons.Default.Warning,
            color = Color(0xFFC61111),
            modifier = Modifier.weight(1f)
        )
        AnalyticsItem(
            label = "Response",
            value = "${state.averageResponseTimeMinutes.toInt()}m",
            icon = Icons.Default.Schedule,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AnalyticsItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = Color(0xFF00695C),
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A202C)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FilterSection(
    selectedStatus: EmergencyStatus?,
    onStatusSelected: (EmergencyStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFFEDF2F7), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        val allStatuses = listOf(null) + EmergencyStatus.entries.take(4) // Take only main statuses to fit better
        
        allStatuses.forEach { status ->
            StatusFilterChip(
                selected = selectedStatus == status,
                label = status?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All",
                onClick = { onStatusSelected(status) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF00695C) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun EmergencyRequestItem(
    request: EmergencyRequest,
    onClick: () -> Unit,
    onAssignClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(request.timestamp))

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = getStatusColor(request.status).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = getStatusColor(request.status),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.patientName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = request.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(status = request.status)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                if (request.status == EmergencyStatus.PENDING) {
                    Button(
                        onClick = onAssignClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC61111),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp).padding(top = 4.dp)
                    ) {
                        Text("Assign", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: EmergencyStatus) {
    val color = getStatusColor(status)
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

private fun getStatusColor(status: EmergencyStatus): Color {
    return when(status) {
        EmergencyStatus.PENDING -> Color(0xFFC61111)
        EmergencyStatus.ASSIGNED -> Color(0xFFE65100)
        EmergencyStatus.EN_ROUTE -> Color(0xFF00695C)
        EmergencyStatus.ARRIVED -> Color(0xFF2E7D32)
        EmergencyStatus.COMPLETED -> Color(0xFF2E7D32)
        EmergencyStatus.CANCELLED -> Color(0xFF546E7A)
    }
}

@Composable
fun RequestDetailsDialog(
    request: EmergencyRequest,
    availableAmbulancesCount: Int,
    onDismiss: () -> Unit,
    onAssignClick: () -> Unit
) {
    val timeAgo = getTimeAgo(request.timestamp)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (request.status == EmergencyStatus.PENDING) {
                Button(
                    onClick = onAssignClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC61111),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Assign Ambulance", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(
                "Request #${request.id.takeLast(6).uppercase()}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A202C)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow("Patient", request.patientName)
                DetailRow("Location", request.location)
                DetailRow("Description", request.description.ifBlank { "No additional details provided." })
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailRow("Reported", timeAgo)
                    DetailRow("Status", status = request.status)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF00695C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$availableAmbulancesCount ambulances available",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun DetailRow(label: String, value: String? = null, status: EmergencyStatus? = null) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
        } else if (status != null) {
            StatusChip(status = status)
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        else -> "${minutes / 60}h ago"
    }
}

@Composable
fun AssignAmbulanceDialog(
    ambulances: List<Ambulance>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit,
    onAmbulanceClick: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Ambulance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A202C)
            )
        },
        text = {
            if (ambulances.isEmpty()) {
                Text("No ambulances are currently available.", color = Color.Red, fontWeight = FontWeight.Bold)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(ambulances) { ambulance ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAssign(ambulance.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFE3F2FD),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = Color(0xFF00695C),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(ambulance.plateNumber, fontWeight = FontWeight.ExtraBold)
                                        Text(ambulance.drivers, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00695C))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
