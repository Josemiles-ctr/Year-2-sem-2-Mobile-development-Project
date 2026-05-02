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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.ui.components.AppLoadingIndicator

private data class HospitalDashboardTab(
    val label: String,
    val icon: ImageVector
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
        HospitalDashboardTab(label = "Dashboard", icon = Icons.Default.Dashboard),
        HospitalDashboardTab(label = "Profile", icon = Icons.Default.AccountCircle)
    )
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
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
                            color = Color(0xFF00695C)
                        )
                    }
                },
                actions = {
                    if (selectedTabIndex == 0) {
                        IconButton(onClick = { showAddAmbulanceDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Ambulance",
                                tint = Color(0xFF00695C)
                            )
                        }
                    }
                    IconButton(onClick = { /* TODO: Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
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
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00695C),
                            selectedTextColor = Color(0xFF00695C),
                            indicatorColor = Color(0xFF00695C).copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        icon = Icons.Default.Warning,
                        color = Color(0xFFC61111),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Available Ambulances",
                        count = uiState.availableAmbulances.size.toString(),
                        icon = Icons.Default.LocalShipping,
                        color = Color(0xFF00695C),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Recent Emergency Requests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AppLoadingIndicator()
                    }
                } else if (uiState.activeRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active emergency requests", color = Color.Gray)
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
                // Hospital Profile Tab
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF00695C).copy(alpha = 0.1f)
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color(0xFF00695C),
                                    modifier = Modifier.padding(12.dp).size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = uiState.hospitalName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("Hospital Administrator", color = Color.Gray)
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
                        
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        
                        ProfileInfoRow(Icons.Default.Info, "Status", "Authorized Provider")
                        ProfileInfoRow(Icons.Default.FlashOn, "Active Requests", uiState.activeRequests.size.toString())
                        ProfileInfoRow(Icons.Default.LocalShipping, "Total Ambulances", uiState.availableAmbulances.size.toString())

                        uiState.error?.let { message ->
                            Text(
                                text = message,
                                color = Color(0xFFC61111),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC61111).copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout System")
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

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
    }
}

@Composable
fun AddAmbulanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var regNo by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add New Ambulance",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(regNo, driverId) },
                enabled = regNo.isNotBlank() && driverId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Ambulance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun AssignmentDialog(
    request: EmergencyRequestEntity,
    availableAmbulances: List<AmbulanceEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onTrackAmbulance: (String) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
        title = {
            Text(
                "Assign Ambulance",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Emergency Request", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(request.description, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(request.location, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                HorizontalDivider(color = Color(0xFFEEEEEE))
                
                Text(
                    "Select available ambulance:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                
                if (availableAmbulances.isEmpty()) {
                    Text(
                        "No ambulances available",
                        color = Color(0xFFC61111),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableAmbulances) { ambulance ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTrackAmbulance(ambulance.id) }
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
                                        Text(ambulance.registrationNo, fontWeight = FontWeight.Bold)
                                        Text("ID: ${ambulance.driverId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                Button(
                                    onClick = { onConfirm(ambulance.id) },
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = title, 
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmergencyRequestItem(
    request: EmergencyRequestEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val statusColor = getStatusColor(request.status)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(10.dp).size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = request.priority.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "#${request.id.takeLast(4).uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
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
                        color = Color.Gray,
                        maxLines = 1
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
                    color = statusColor.copy(alpha = 0.1f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

private fun getStatusColor(status: String): Color {
    return when(status.uppercase()) {
        "PENDING" -> Color(0xFFC61111)
        "ASSIGNED" -> Color(0xFFE65100)
        "EN_ROUTE" -> Color(0xFF1565C0)
        "ARRIVED" -> Color(0xFF00838F)
        "COMPLETED" -> Color(0xFF2E7D32)
        else -> Color(0xFF546E7A)
    }
}
