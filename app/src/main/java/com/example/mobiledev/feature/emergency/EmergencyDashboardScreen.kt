package com.example.mobiledev.feature.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(Color(0xFFF5F5F5))
    ) {
        AnalyticsSummary(state)
        
        FilterSection(
            selectedStatus = state.statusFilter,
            onStatusSelected = { onEvent(EmergencyDashboardEvent.FilterByStatus(it)) }
        )

        if (state.isLoading && state.requests.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                color = Color(0xFFC61111),
                trackColor = Color(0xFFC61111).copy(alpha = 0.1f)
            )
        }

        if (state.isLoading && state.requests.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

        state.error?.let { errorMsg ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Text(errorMsg)
            }
        }
    }
}

@Composable
fun AnalyticsSummary(state: EmergencyDashboardState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalyticsItem(
            label = "Total Today",
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
            label = "Avg Response",
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FilterSection(
    selectedStatus: EmergencyStatus?,
    onStatusSelected: (EmergencyStatus?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "Filter by Status",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                StatusFilterChip(
                    selected = selectedStatus == null,
                    label = "All",
                    onClick = { onStatusSelected(null) }
                )
            }
            items(EmergencyStatus.entries) { status ->
                StatusFilterChip(
                    selected = selectedStatus == status,
                    label = status.name.replace("_", " ").lowercase().replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    onClick = { onStatusSelected(status) }
                )
            }
        }
    }
}

@Composable
fun StatusFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0xFF00695C) else Color.White,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFEEEEEE)),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = if (selected) Color.White else Color.Gray
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = getStatusColor(request.status).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = getStatusColor(request.status),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.patientName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${request.id.takeLast(4).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = request.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(status = request.status)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                if (request.status == EmergencyStatus.PENDING) {
                    TextButton(
                        onClick = onAssignClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Assign",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFF00796B),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Received",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
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
        contentColor = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

private fun getStatusColor(status: EmergencyStatus): Color {
    return when(status) {
        EmergencyStatus.PENDING -> Color(0xFFC61111)
        EmergencyStatus.ASSIGNED -> Color(0xFFE65100)
        EmergencyStatus.EN_ROUTE -> Color(0xFF1565C0)
        EmergencyStatus.ARRIVED -> Color(0xFF00838F)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC61111)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Assign Ambulance")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
        title = {
            Text(
                "Request #${request.id.takeLast(6).uppercase()}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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

                HorizontalDivider(color = Color(0xFFEEEEEE))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF00695C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$availableAmbulancesCount ambulances available",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
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
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            if (ambulances.isEmpty()) {
                Text("No ambulances are currently available.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(ambulances) { ambulance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAmbulanceClick(ambulance.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE0F2F1),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFF00695C),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(ambulance.plateNumber, fontWeight = FontWeight.Bold)
                                    Text(ambulance.driverName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Button(
                                onClick = { onAssign(ambulance.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Assign", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun EmergencyDashboardPreview() {
    val mockState = EmergencyDashboardState(
        requests = listOf(
            EmergencyRequest(
                id = "REQ-1234",
                patientName = "Joseph O",
                location = "123 Main St, Nairobi",
                phoneNumber = "0712345678",
                description = "Chest pain and difficulty breathing",
                status = EmergencyStatus.PENDING,
                priority = EmergencyPriority.HIGH,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5
            )
        ),
        filteredRequests = listOf(
            EmergencyRequest(
                id = "REQ-1234",
                patientName = "Joseph O",
                location = "123 Main St, Nairobi",
                phoneNumber = "0712345678",
                description = "Chest pain and difficulty breathing",
                status = EmergencyStatus.PENDING,
                priority = EmergencyPriority.HIGH,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5
            )
        ),
        totalRequestsToday = 12,
        pendingRequestsCount = 3,
        averageResponseTimeMinutes = 8.5,
        newRequestsCount = 1
    )
    MaterialTheme {
        EmergencyDashboardContent(state = mockState, onEvent = {})
    }
}
