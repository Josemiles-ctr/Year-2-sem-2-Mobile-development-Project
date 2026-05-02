package com.example.mobiledev.feature.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.R
import com.example.mobiledev.data.model.*
import com.example.mobiledev.ui.components.AppBackgroundContainer
import com.example.mobiledev.ui.components.AppLoadingIndicator
import com.example.mobiledev.ui.components.GlassyCard
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
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent
    ) { padding ->
        AppBackgroundContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 24.dp)) {
                AnalyticsSummary(state)
                
                FilterSection(
                    selectedStatus = state.statusFilter,
                    onStatusSelected = { onEvent(EmergencyDashboardEvent.FilterByStatus(it)) }
                )

                if (state.isLoading && state.requests.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                    )
                }

                if (state.isLoading && state.requests.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

            state.error?.let { errorMsg ->
                GlassyCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f)
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
    GlassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
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
    val valueColor = if (color == Color.Unspecified) {
        MaterialTheme.colorScheme.onSurface
    } else {
        glassReadableAccent(color)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FilterSection(
    selectedStatus: EmergencyStatus?,
    onStatusSelected: (EmergencyStatus?) -> Unit
) {
    GlassyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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

    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
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
    val baseColor = when(status) {
        EmergencyStatus.PENDING -> Color(0xFFD32F2F)
        EmergencyStatus.ASSIGNED -> Color(0xFFE65100)
        EmergencyStatus.EN_ROUTE -> Color(0xFF1565C0)
        EmergencyStatus.ARRIVED -> Color(0xFF00838F)
        EmergencyStatus.COMPLETED -> Color(0xFF2E7D32)
        EmergencyStatus.CANCELLED -> Color(0xFF546E7A)
    }
    val color = glassReadableAccent(baseColor)

    Surface(
        color = color.copy(alpha = 0.18f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.95f))
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun glassReadableAccent(color: Color): Color {
    val luminance = color.luminance()
    return when {
        luminance > 0.58f -> lerp(color, Color.Black, 0.42f)
        luminance < 0.18f -> lerp(color, Color.White, 0.18f)
        else -> color
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Emergency Request Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    if (request.status == EmergencyStatus.PENDING) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onAssignClick) {
                            Text("Assign Ambulance")
                        }
                    }
                }
            }
        }
    }
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
    onAssign: (String) -> Unit,
    onAmbulanceClick: (String) -> Unit = {}
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Assign Ambulance",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (ambulances.isEmpty()) {
                    Text(
                        "No available ambulances found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(ambulances) { ambulance ->
                            ListItem(
                                headlineContent = { Text(ambulance.plateNumber) },
                                supportingContent = { Text("Driver: ${ambulance.driverName}") },
                                modifier = Modifier.clickable { onAmbulanceClick(ambulance.id) },
                                trailingContent = {
                                    Button(onClick = { onAssign(ambulance.id) }) {
                                        Text("Assign")
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        }
    }
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
