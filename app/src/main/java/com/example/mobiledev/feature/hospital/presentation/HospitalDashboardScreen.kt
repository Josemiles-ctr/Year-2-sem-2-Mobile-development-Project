package com.example.mobiledev.feature.hospital.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.ui.components.AppBackgroundContainer
import com.example.mobiledev.ui.components.GlassyCard
import com.example.mobiledev.ui.components.AppLoadingIndicator
import java.util.concurrent.TimeUnit


data class HospitalDashboardTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class TriageStats(
    val critical: Int,
    val serious: Int,
    val stable: Int
) {
    val total: Int get() = critical + serious + stable
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalDashboardScreen(
    viewModel: HospitalDashboardViewModel,
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onAmbulanceClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAmbulanceDialog by remember { mutableStateOf(false) }
    var selectedStatusFilter by rememberSaveable { mutableStateOf<String?>(null) }
    
    val tabs = listOf(
        HospitalDashboardTab(label = "Triage", icon = Icons.Default.Warning),
        HospitalDashboardTab(label = "Drivers", icon = Icons.Default.DirectionsCar),
        HospitalDashboardTab(label = "Patients", icon = Icons.Default.People),
        HospitalDashboardTab(label = "Reports", icon = Icons.Default.Assessment),
        HospitalDashboardTab(label = "Profile", icon = Icons.Default.Person)
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    
    // Calculate triage stats based on requests
    val triageStats = remember(uiState.activeRequests) {
        val critical = uiState.activeRequests.count { it.priority.uppercase() == "CRITICAL" }
        val serious = uiState.activeRequests.count { it.priority.uppercase() in listOf("HIGH", "SERIOUS") }
        val stable = uiState.activeRequests.count { it.priority.uppercase() in listOf("MEDIUM", "LOW", "STABLE") }
        TriageStats(critical, serious, stable)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Logo Placeholder
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        
                        // Notifications
                        IconButton(onClick = onNotificationsClick) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotificationsCount > 0) {
                                        Badge { Text(unreadNotificationsCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label, modifier = Modifier.size(24.dp)) },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        AppBackgroundContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Screen Title
                Text(
                    text = "Triage Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1), // Hardcoded blue to match screenshot
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 28.sp
                )

                when (selectedTabIndex) {
                    0 -> TriageDashboardContent(
                        uiState = uiState,
                        triageStats = triageStats,
                        selectedStatusFilter = selectedStatusFilter,
                        onStatusFilterChange = { selectedStatusFilter = it },
                        onRequestSelected = { viewModel.onRequestSelected(it) },
                        onAddAmbulanceClick = { showAddAmbulanceDialog = true }
                    )
                    1 -> DriversTabContent(
                        ambulances = uiState.availableAmbulances,
                        isLoading = uiState.isLoading
                    )
                    2 -> PatientsTabContent(
                        activeRequests = uiState.activeRequests,
                        isLoading = uiState.isLoading,
                        onRequestSelected = { viewModel.onRequestSelected(it) }
                    )
                    3 -> ReportsTabContent(
                        activeRequests = uiState.activeRequests,
                        totalAmbulances = uiState.availableAmbulances.size
                    )
                    4 -> ProfileTabContent(
                        hospitalName = uiState.hospitalName,
                        onLogoutClick = onLogoutClick
                    )
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
fun TriageDashboardContent(
    uiState: HospitalDashboardUiState,
    triageStats: TriageStats,
    selectedStatusFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    onRequestSelected: (EmergencyRequestEntity) -> Unit,
    onAddAmbulanceClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Active Cases Summary
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            containerColor = Color(0xFFE3F2FD) // Light blue background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Cases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1), // Dark blue for readability
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val contentColor = Color(0xFF1A1C1E) // Dark grey for content visibility
                    
                    // Total
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total: ${triageStats.total}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                    
                    // Critical
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.Red,
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Critical: ${triageStats.critical}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor
                            )
                        }
                    }
                    
                    // Serious
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFFFA500),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Serious: ${triageStats.serious}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor
                            )
                        }
                    }
                    
                    // Stable
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.Green,
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.size(12.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Stable: ${triageStats.stable}",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }

        // Status Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityFilterButton(
                label = "Critical (${triageStats.critical})",
                selected = selectedStatusFilter == "CRITICAL",
                onClick = { onStatusFilterChange(if (selectedStatusFilter == "CRITICAL") null else "CRITICAL") },
                selectedColor = Color(0xFFD32F2F),
                modifier = Modifier.weight(1f)
            )
            PriorityFilterButton(
                label = "Serious (${triageStats.serious})",
                selected = selectedStatusFilter == "SERIOUS",
                onClick = { onStatusFilterChange(if (selectedStatusFilter == "SERIOUS") null else "SERIOUS") },
                selectedColor = Color(0xFFFFA500),
                modifier = Modifier.weight(1f)
            )
            PriorityFilterButton(
                label = "Stable (${triageStats.stable})",
                selected = selectedStatusFilter == "STABLE",
                onClick = { onStatusFilterChange(if (selectedStatusFilter == "STABLE") null else "STABLE") },
                selectedColor = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        // Requests Title
        Text(
            text = "${selectedStatusFilter?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All Active"} Requests",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Requests List
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else if (uiState.activeRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active emergency requests", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            val filteredRequests = when(selectedStatusFilter) {
                "CRITICAL" -> uiState.activeRequests.filter { it.priority.uppercase() == "CRITICAL" }
                "SERIOUS" -> uiState.activeRequests.filter { it.priority.uppercase() in listOf("HIGH", "SERIOUS") }
                "STABLE" -> uiState.activeRequests.filter { it.priority.uppercase() in listOf("MEDIUM", "LOW", "STABLE") }
                else -> uiState.activeRequests
            }

            if (filteredRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ${selectedStatusFilter?.lowercase() ?: ""} requests", color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRequests) { request ->
                        TriageRequestItem(
                            request = request,
                            onClick = { onRequestSelected(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        color = if (selected) selectedColor else Color(0xFFEEEEEE),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun TriageRequestItem(
    request: EmergencyRequestEntity,
    onClick: () -> Unit
) {
    val timeAgo = getTimeAgoString(request.createdAt)
    val contentColor = Color(0xFF1A1C1E) // Guaranteed dark color for visibility
    
    val priorityColor = when(request.priority.uppercase()) {
        "CRITICAL" -> Color.Red
        "HIGH", "SERIOUS" -> Color(0xFFFFA500)
        "MEDIUM", "LOW", "STABLE" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    
    val priorityLabel = when(request.priority.uppercase()) {
        "CRITICAL" -> "Critical"
        "HIGH", "SERIOUS" -> "Serious"
        "MEDIUM", "LOW", "STABLE" -> "Stable"
        else -> request.priority
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Priority Badge
            Surface(
                color = priorityColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = priorityLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Patient name
            Row {
                Text("Patient: ", color = contentColor)
                Text(
                    text = request.location.split(",").firstOrNull() ?: request.location,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            
            // Incident description
            Row {
                Text("Incident: ", color = contentColor)
                Text(
                    text = request.description,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            
            // Time elapsed
            Row {
                Text("Time Elapsed: ", color = contentColor)
                Text(
                    text = "$timeAgo",
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.5f)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF80DEEA) // Cyan color from screenshot
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun DriversTabContent(
    ambulances: List<AmbulanceEntity>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ambulance Fleet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else if (ambulances.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No ambulances available", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(ambulances) { ambulance ->
                    AmbulanceDriverCard(ambulance = ambulance)
                }
            }
        }
    }
}

@Composable
fun AmbulanceDriverCard(ambulance: AmbulanceEntity) {
    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ambulance.registrationNo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Driver ID: ${ambulance.driverId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Status indicator
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.18f),
                    contentColor = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "AVAILABLE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PatientsTabContent(
    activeRequests: List<EmergencyRequestEntity>,
    isLoading: Boolean,
    onRequestSelected: (EmergencyRequestEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Active Patients",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else if (activeRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active patients", color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeRequests) { request ->
                    PatientCard(
                        request = request,
                        onClick = { onRequestSelected(request) }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    request: EmergencyRequestEntity,
    onClick: () -> Unit
) {
    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Request #${request.id.takeLast(4)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = request.location,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Priority badge
                val priorityColor = when(request.priority.uppercase()) {
                    "CRITICAL" -> Color(0xFFD32F2F)
                    "HIGH", "SERIOUS" -> Color(0xFFFFA500)
                    else -> Color(0xFF4CAF50)
                }
                Surface(
                    color = priorityColor.copy(alpha = 0.18f),
                    contentColor = priorityColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = request.priority,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2
            )
        }
    }
}

@Composable
fun ReportsTabContent(
    activeRequests: List<EmergencyRequestEntity>,
    totalAmbulances: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Emergency Reports",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary statistics
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ReportStatCard(
                    title = "Total Active Requests",
                    value = activeRequests.size.toString(),
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                )
            }

            item {
                ReportStatCard(
                    title = "Total Ambulances",
                    value = totalAmbulances.toString(),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            item {
                val pendingCount = activeRequests.count { it.status.uppercase() == "PENDING" }
                ReportStatCard(
                    title = "Pending Requests",
                    value = pendingCount.toString(),
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }

            item {
                val assignedCount = activeRequests.count { it.status.uppercase() in listOf("ASSIGNED", "EN_ROUTE") }
                ReportStatCard(
                    title = "In Progress",
                    value = assignedCount.toString(),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            item {
                val criticalCount = activeRequests.count { it.priority.uppercase() == "CRITICAL" }
                ReportStatCard(
                    title = "Critical Cases",
                    value = criticalCount.toString(),
                    backgroundColor = Color(0xFFD32F2F).copy(alpha = 0.18f)
                )
            }

            item {
                val avgResponseTime = if (activeRequests.isNotEmpty()) {
                    activeRequests.mapNotNull { it.estimatedTimeMins }.average().toInt()
                } else {
                    0
                }
                ReportStatCard(
                    title = "Avg Response Time",
                    value = "${avgResponseTime} min",
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@Composable
fun ReportStatCard(
    title: String,
    value: String,
    backgroundColor: Color
) {
    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        containerColor = backgroundColor.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProfileTabContent(
    hospitalName: String,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Name: $hospitalName",
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
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

private fun getTimeAgoString(timestamp: Long): String {
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
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select available ambulance:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (availableAmbulances.isEmpty()) {
                        val errorColor = glassReadableAccent(MaterialTheme.colorScheme.error)
                        Text(
                            "No ambulances available",
                            color = errorColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(availableAmbulances) { ambulance ->
                                ListItem(
                                    headlineContent = { Text(ambulance.registrationNo, color = MaterialTheme.colorScheme.onSurface) },
                                    supportingContent = { Text("Driver ID: ${ambulance.driverId}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) },
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

private fun glassReadableAccent(color: Color): Color {
    val luminance = color.luminance()
    return when {
        luminance > 0.65f -> androidx.compose.ui.graphics.lerp(color, Color.Black, 0.45f)
        luminance < 0.15f -> androidx.compose.ui.graphics.lerp(color, Color.White, 0.25f)
        else -> color
    }
}
