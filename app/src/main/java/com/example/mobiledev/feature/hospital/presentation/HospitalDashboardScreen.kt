package com.example.mobiledev.feature.hospital.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.ui.components.AppBackgroundContainer
import com.example.mobiledev.ui.components.GlassyCard
import com.example.mobiledev.ui.components.AppLoadingIndicator


private data class HospitalDashboardTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDashboardScreen(
    viewModel: HospitalDashboardViewModel,
    onLogoutClick: () -> Unit = {},
    onAmbulanceClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAmbulanceDialog by remember { mutableStateOf(false) }
    val tabs = listOf(
        HospitalDashboardTab(label = "Dashboard", icon = Icons.Default.Notifications),
        HospitalDashboardTab(label = "Hospital", icon = Icons.Default.AccountCircle)
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = uiState.hospitalName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedTabIndex == 0) "Emergency Dashboard" else "Hospital Information",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        actions = {
                            if (selectedTabIndex == 0) {
                                IconButton(onClick = {
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Ambulance",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { /* TODO: Notifications */ }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        bottomBar = {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        AppBackgroundContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    // Dashboard tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Active Requests",
                            count = uiState.activeRequests.size.toString(),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Available Ambulances",
                            count = uiState.availableAmbulances.size.toString(),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Recent Emergency Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AppLoadingIndicator()
                        }
                    } else if (uiState.activeRequests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No active emergency requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.activeRequests) { request ->
                                EmergencyRequestItem(
                                    request = request,
                                    onClick = { viewModel.onRequestSelected(request) }
                                )
                            }
                        }
                    }
                } else {
                    // Hospital tab
                    GlassyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Hospital Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Name: ${uiState.hospitalName}")
                            Text("Active Requests: ${uiState.activeRequests.size}")
                            Text("Available Ambulances: ${uiState.availableAmbulances.size}")
                            uiState.error?.let { message ->
                                Text(
                                    text = message,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = onLogoutClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.88f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logout")
                            }
                        }
                    }
                }
            }

            // Assignment Dialog (Dispatch Flow)
            uiState.selectedRequest?.let { request ->
                AssignmentDialog(
                    request = request,
                    availableAmbulances = uiState.availableAmbulances,
                    onDismiss = { viewModel.onRequestSelected(null) },
                    onConfirm = { ambulanceId ->
                        viewModel.assignAmbulance(request.id, ambulanceId)
                        viewModel.onRequestSelected(null)
                    },
                    onTrackAmbulance = { ambulanceId ->
                        viewModel.onRequestSelected(null)
                        onAmbulanceClick(ambulanceId)
                    }
                )
            }

            // Add Ambulance Dialog
            if (showAddAmbulanceDialog) {
                AddAmbulanceDialog(
                    onDismiss = { showAddAmbulanceDialog = false },
                    onConfirm = { regNo, driverId ->
                        viewModel.addAmbulance(regNo, driverId)
                        showAddAmbulanceDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddAmbulanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var regNo by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }

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
                    text = "Add New Ambulance",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it },
                    label = { Text("Registration Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = driverId,
                    onValueChange = { driverId = it },
                    label = { Text("Driver ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(regNo, driverId) },
                        enabled = regNo.isNotBlank() && driverId.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AssignmentDialog(
    request: EmergencyRequestEntity,
    availableAmbulances: List<AmbulanceEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onTrackAmbulance: (String) -> Unit = {}
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

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Emergency: ${request.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Location: ${request.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select available ambulance:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (availableAmbulances.isEmpty()) {
                        Text(
                            "No ambulances available",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(availableAmbulances) { ambulance ->
                                ListItem(
                                    headlineContent = { Text(ambulance.registrationNo) },
                                    supportingContent = { Text("Driver ID: ${ambulance.driverId}") },
                                    modifier = Modifier.clickable { onTrackAmbulance(ambulance.id) },
                                    trailingContent = {
                                        Button(onClick = { onConfirm(ambulance.id) }) {
                                            Text("Assign")
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
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

@Composable
fun StatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassyCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        containerColor = color.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EmergencyRequestItem(
    request: EmergencyRequestEntity,
    onClick: () -> Unit
) {
    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when (request.priority.uppercase()) {
                            "CRITICAL" -> Color.Red
                            "HIGH" -> Color.Red
                            "MEDIUM" -> Color(0xFFFFA500)
                            else -> Color.Green
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.priority,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = request.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val baseColor = when(request.status.uppercase()) {
                    "PENDING" -> Color(0xFFD32F2F)
                    "ASSIGNED" -> Color(0xFFE65100)
                    else -> Color(0xFF2E7D32)
                }
                val chipColor = glassReadableAccent(baseColor)
                Surface(
                    color = chipColor.copy(alpha = 0.18f),
                    contentColor = chipColor,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, chipColor.copy(alpha = 0.95f))
                ) {
                    Text(
                        text = request.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun glassReadableAccent(color: Color): Color {
    val luminance = color.luminance()
    return when {
        luminance > 0.58f -> androidx.compose.ui.graphics.lerp(color, Color.Black, 0.42f)
        luminance < 0.18f -> androidx.compose.ui.graphics.lerp(color, Color.White, 0.18f)
        else -> color
    }
}
