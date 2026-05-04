package com.example.mobiledev.feature.hospital.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
    
    val triageStats = remember(uiState.activeRequests) {
        val critical = uiState.activeRequests.count { it.priority.uppercase() == "CRITICAL" }
        val serious = uiState.activeRequests.count { it.priority.uppercase() in listOf("HIGH", "SERIOUS") }
        val stable = uiState.activeRequests.count { it.priority.uppercase() in listOf("MEDIUM", "LOW", "STABLE") }
        TriageStats(critical, serious, stable)
    }

    Scaffold(
        containerColor = Color(0xFFFBFBFB),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.hospitalName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Hospital Management Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                                tint = Color(0xFF00695C)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "Triage Dashboard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Prioritize and manage active emergency requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Active Cases Summary
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Cases Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00695C)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SummaryItem(label = "Total", value = "${triageStats.total}", modifier = Modifier.weight(1f))
                        SummaryItem(label = "Critical", value = "${triageStats.critical}", color = Color.Red, modifier = Modifier.weight(1f))
                        SummaryItem(label = "Serious", value = "${triageStats.serious}", color = Color(0xFFFFA500), modifier = Modifier.weight(1f))
                        SummaryItem(label = "Stable", value = "${triageStats.stable}", color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Filter Buttons
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0xFFEDF2F7), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                PriorityFilterButton(
                    label = "Critical",
                    selected = selectedStatusFilter == "CRITICAL",
                    onClick = { onStatusFilterChange(if (selectedStatusFilter == "CRITICAL") null else "CRITICAL") },
                    selectedColor = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
                PriorityFilterButton(
                    label = "Serious",
                    selected = selectedStatusFilter == "SERIOUS",
                    onClick = { onStatusFilterChange(if (selectedStatusFilter == "SERIOUS") null else "SERIOUS") },
                    selectedColor = Color(0xFFFFA500),
                    modifier = Modifier.weight(1f)
                )
                PriorityFilterButton(
                    label = "Stable",
                    selected = selectedStatusFilter == "STABLE",
                    onClick = { onStatusFilterChange(if (selectedStatusFilter == "STABLE") null else "STABLE") },
                    selectedColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else {
            val filteredRequests = when(selectedStatusFilter) {
                "CRITICAL" -> uiState.activeRequests.filter { it.priority.uppercase() == "CRITICAL" }
                "SERIOUS" -> uiState.activeRequests.filter { it.priority.uppercase() in listOf("HIGH", "SERIOUS") }
                "STABLE" -> uiState.activeRequests.filter { it.priority.uppercase() in listOf("MEDIUM", "LOW", "STABLE") }
                else -> uiState.activeRequests
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRequests) { request ->
                    TriageDashboardItem(
                        request = request,
                        onClick = { onRequestSelected(request) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
        modifier = modifier.fillMaxHeight(),
        color = if (selected) selectedColor else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label, 
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun TriageDashboardItem(
    request: EmergencyRequestEntity,
    onClick: () -> Unit
) {
    val timeAgo = getTimeAgoString(request.createdAt)
    
    val (priorityLabel, priorityColor) = when(request.priority.uppercase()) {
        "CRITICAL" -> "Critical" to Color(0xFFD32F2F)
        "HIGH", "SERIOUS" -> "Serious" to Color(0xFFFFA500)
        "MEDIUM", "LOW", "STABLE" -> "Stable" to Color(0xFF2E7D32)
        else -> request.priority to Color.Gray
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = priorityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = priorityLabel,
                        color = priorityColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Emergency #${request.id.takeLast(4)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A202C)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00695C),
                    contentColor = Color.White
                )
            ) {
                Text("View Details", fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "Fleet Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Monitor and manage active ambulances",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF00695C),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ambulance.registrationNo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Driver: ${ambulance.driverId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ambulance.status.uppercase(),
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
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
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "Patient Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Track and manage patient emergency history",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppLoadingIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
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
    val (priorityLabel, priorityColor) = when(request.priority.uppercase()) {
        "CRITICAL" -> "Critical" to Color(0xFFD32F2F)
        "HIGH", "SERIOUS" -> "Serious" to Color(0xFFFFA500)
        else -> "Stable" to Color(0xFF2E7D32)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = priorityColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = priorityColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Patient #${request.id.takeLast(4)}",
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
            }
            
            Surface(
                color = priorityColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = priorityLabel,
                    color = priorityColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ReportsTabContent(
    activeRequests: List<EmergencyRequestEntity>,
    totalAmbulances: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "Analytics & Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Monitor performance and efficiency metrics",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val pendingCount = activeRequests.count { it.status.uppercase() == "PENDING" }
        val criticalCount = activeRequests.count { it.priority.uppercase() == "CRITICAL" }
        
        val stats = listOf(
            HospitalReportStat("Total Requests", activeRequests.size.toString(), Icons.Default.Assessment, Color(0xFF00695C)),
            HospitalReportStat("Fleet Size", totalAmbulances.toString(), Icons.Default.DirectionsCar, Color(0xFF00695C)),
            HospitalReportStat("Pending", pendingCount.toString(), Icons.Default.Warning, Color(0xFFFFA000)),
            HospitalReportStat("Critical", criticalCount.toString(), Icons.Default.Notifications, Color(0xFFD32F2F))
        )

        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats.size) { index ->
                val stat = stats[index]
                ReportStatCard(stat)
            }
        }
    }
}

private data class HospitalReportStat(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
private fun ReportStatCard(stat: HospitalReportStat) {
    ElevatedCard(
        modifier = Modifier.height(160.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = stat.color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = null,
                        tint = stat.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileTabContent(
    hospitalName: String,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "Hospital Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Manage facility details and sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFFF1F5F9),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = hospitalName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A202C)
                            )
                            Text(
                                text = "Verified Medical Facility",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC61111),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Logout from Session", fontWeight = FontWeight.Bold)
                    }
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Ambulance",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1A202C),
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = regNo,
                    onValueChange = { regNo = it },
                    label = { Text("Registration Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = driverId,
                    onValueChange = { driverId = it },
                    label = { Text("Driver ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(regNo, driverId) },
                        enabled = regNo.isNotBlank() && driverId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00695C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Ambulance")
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Assign Ambulance",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1A202C),
                    fontWeight = FontWeight.ExtraBold
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Emergency: ${request.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        "Location: ${request.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select available ambulance:",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (availableAmbulances.isEmpty()) {
                        Text(
                            "No ambulances available",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                            items(availableAmbulances) { ambulance ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onConfirm(ambulance.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(ambulance.registrationNo, fontWeight = FontWeight.Bold)
                                            Text("Driver: ${ambulance.driverId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF00695C))
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
                }
            }
        }
    }
}
