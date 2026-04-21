package com.example.mobiledev.feature.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.R
import com.example.mobiledev.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun EmergencyDashboardScreen(
    viewModel: EmergencyViewModel
) {
    val state = viewModel.uiState.collectAsState().value
    EmergencyDashboardContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyDashboardContent(
    state: EmergencyDashboardState,
    onEvent: (EmergencyDashboardEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0C2C39).copy(alpha = 0.26f),
                                Color.Transparent,
                                Color(0xFF0C2C39).copy(alpha = 0.34f)
                            )
                        )
                    )
            )
            Column(modifier = Modifier.fillMaxSize()) {
                AnalyticsSummary(state)
                
                FilterSection(
                    selectedStatus = state.statusFilter,
                    onStatusSelected = { onEvent(EmergencyDashboardEvent.FilterByStatus(it)) }
                )

                if (state.isLoading && state.requests.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        "No requests found matching criteria.",
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
                    }
                )
            }

            state.error?.let { errorMsg ->
                ElevatedCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f)
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.large
                        ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsSummary(state: EmergencyDashboardState) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.large
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AnalyticsItem("Total Today", state.totalRequestsToday.toString())
            AnalyticsItem("Pending", state.pendingRequestsCount.toString(), color = Color.Red)
            AnalyticsItem("Avg Response", "${state.averageResponseTimeMinutes}m")
        }
    }
}

@Composable
fun AnalyticsItem(label: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun FilterSection(
    selectedStatus: EmergencyStatus?,
    onStatusSelected: (EmergencyStatus?) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.large
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "Filter by Status",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { onStatusSelected(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.36f),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                items(EmergencyStatus.entries) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusSelected(status) },
                        label = { Text(status.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.36f),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
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
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${request.id.takeLast(4).uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                request.patientName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "Location: ${request.location}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(status = request.status)
                Spacer(modifier = Modifier.weight(1f))
                if (request.status == EmergencyStatus.PENDING) {
                    TextButton(
                        onClick = onAssignClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: EmergencyStatus) {
    val color = when(status) {
        EmergencyStatus.PENDING -> Color.Red
        EmergencyStatus.ASSIGNED -> Color(0xFFFBC02D) // Yellow/Orange
        EmergencyStatus.EN_ROUTE -> Color.Blue
        EmergencyStatus.ARRIVED -> Color.Cyan
        EmergencyStatus.COMPLETED -> Color.Green
        EmergencyStatus.CANCELLED -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.9f))
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
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
        title = { Text("Emergency Request Details", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Request ID", request.id)
                DetailRow("Status", request.status.name)
                DetailRow("Patient Name", request.patientName)
                DetailRow("Patient Callback Number", request.phoneNumber.ifBlank { "Not available" })
                DetailRow("Patient Location", request.location)
                DetailRow("Medical Notes", request.description.ifBlank { "No notes provided" })
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailRow("Request Time", SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(request.timestamp)))
                    DetailRow("Request Age", timeAgo)
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Available Ambulances: $availableAmbulancesCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            if (request.status == EmergencyStatus.PENDING) {
                Button(onClick = onAssignClick) {
                    Text("Assign Ambulance")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        else -> "${minutes / 60}h ${minutes % 60}m ago"
    }
}

@Composable
fun AssignAmbulanceDialog(
    ambulances: List<Ambulance>,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Ambulance") },
        text = {
            if (ambulances.isEmpty()) {
                Text("No available ambulances found.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(ambulances) { ambulance ->
                        ListItem(
                            headlineContent = { Text(ambulance.plateNumber) },
                            supportingContent = { Text("Driver: ${ambulance.driverName}") },
                            trailingContent = {
                                Button(onClick = { onAssign(ambulance.id) }) {
                                    Text("Assign")
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
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
