package com.example.mobiledev.feature.patient.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mobiledev.data.location.Coordinates
import com.example.mobiledev.data.location.DeviceLocationProvider
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.ui.components.FullScreenLoading
import com.example.mobiledev.ui.components.GlassyCard
import java.util.Locale

@Composable
fun PatientHospitalDetailsRoute(
    viewModel: PatientHospitalDetailsViewModel,
    patientHospitalsViewModel: PatientHospitalsViewModel,
    onBackClick: () -> Unit,
    onAmbulanceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationProvider = remember(context) { DeviceLocationProvider(context) }
    val patientUiState by patientHospitalsViewModel.uiState.collectAsState()
    val currentLocation = patientUiState.currentLocation

    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            permissionRequested = true
        }
    }

    androidx.compose.runtime.LaunchedEffect(permissionRequested) {
        if (permissionRequested) {
            val coords = locationProvider.getCurrentCoordinates()
            patientHospitalsViewModel.updateLocation(coords)
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            permissionRequested = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    PatientHospitalDetailsScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        onAmbulanceClick = onAmbulanceClick,
        currentLocation = currentLocation,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHospitalDetailsScreen(
    viewModel: PatientHospitalDetailsViewModel,
    onBackClick: () -> Unit,
    onAmbulanceClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    currentLocation: Coordinates? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val hospital = uiState.hospital

    Scaffold(
        modifier = modifier.testTag("patientHospitalDetailsRoot"),
        containerColor = Color(0xFFFBFBFB),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFC61111)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hospital?.name ?: "Hospital Details",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )
                        Text(
                            text = if (uiState.isHospitalOffline) "Facility currently offline" else "Active medical facility",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.isHospitalOffline) Color.Red else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(Modifier.padding(innerPadding))
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )
            hospital != null -> DetailsContent(
                hospital = hospital,
                ambulances = uiState.ambulances,
                availableAmbulanceCount = uiState.availableAmbulanceCount,
                isHospitalOffline = uiState.isHospitalOffline,
                isSubmittingRequest = uiState.isSubmittingRequest,
                submitErrorMessage = uiState.submitErrorMessage,
                submitSuccessMessage = uiState.submitSuccessMessage,
                currentLocation = currentLocation,
                onSubmitEmergencyRequest = { desc, ambId ->
                    viewModel.submitEmergencyRequest(
                        desc,
                        currentLocation?.latitude,
                        currentLocation?.longitude,
                        ambId
                    )
                },
                onDismissSubmitMessage = viewModel::clearSubmitMessage,
                onBackClick = onBackClick,
                onAmbulanceClick = onAmbulanceClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB)),
        contentAlignment = Alignment.Center
    ) {
        FullScreenLoading()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onBackClick: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Unable to load hospital details",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onBackClick) {
                    Text("Back", color = Color(0xFF00695C))
                }
            }
        }
    }
}

@Composable
private fun DetailsContent(
    hospital: HospitalEntity,
    ambulances: List<AmbulanceEntity>,
    availableAmbulanceCount: Int,
    isHospitalOffline: Boolean,
    isSubmittingRequest: Boolean,
    submitErrorMessage: String?,
    submitSuccessMessage: String?,
    currentLocation: Coordinates?,
    onSubmitEmergencyRequest: (String, String?) -> Unit,
    onDismissSubmitMessage: () -> Unit,
    onBackClick: () -> Unit,
    onAmbulanceClick: (String) -> Unit,
    modifier: Modifier
) {
    var selectedAmbulanceId by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ... (Hospital Info Card and Ambulances Header)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hospital.name,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        letterSpacing = (-1).sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = hospital.location,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "LEVEL 1\nTRAUMA",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            lineHeight = 12.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFF5F5F5))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            val distanceKm = remember(hospital.id, currentLocation) {
                                hospitalDistanceKm(hospital, currentLocation)
                            }
                            val distanceDisplay = distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "Nearby"
                            val isBusy = hospital.activeAmbulances < 2

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CURRENT STATUS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = if (isBusy) "Busy" else "Available",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBusy) Color(0xFFC62828) else Color(0xFF00695C)
                                    )
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "APPROX DISTANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = distanceDisplay,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Available Ambulances Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Ambulances",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        text = "$availableAmbulanceCount Units Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            if (ambulances.isEmpty()) {
                item {
                    Text(
                        text = "No ambulances available at this moment.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                items(ambulances) { ambulance ->
                    AmbulanceCard(
                        ambulance = ambulance,
                        isSelected = selectedAmbulanceId == ambulance.id,
                        currentLocation = currentLocation,
                        onClick = { selectedAmbulanceId = ambulance.id }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Bottom Action Section
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.95f))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (selectedAmbulanceId != null) {
                        onSubmitEmergencyRequest("Emergency Request", selectedAmbulanceId)
                        onAmbulanceClick(selectedAmbulanceId!!)
                    }
                },
                enabled = selectedAmbulanceId != null && !isSubmittingRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("confirmSelectionButton"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC61111),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                val selectedReg = ambulances.find { it.id == selectedAmbulanceId }?.registrationNo?.take(7) ?: ""
                Text(
                    text = if (selectedAmbulanceId == null) "Select an Ambulance" 
                           else "Confirm Selection ($selectedReg)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Emergency dispatch will be notified\nimmediately upon confirmation.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Composable
private fun AmbulanceCard(
    ambulance: AmbulanceEntity,
    isSelected: Boolean,
    currentLocation: Coordinates? = null,
    onClick: () -> Unit
) {
    val type = remember(ambulance.id) {
        listOf("Advanced Life Support", "Basic Life Support", "Paramedic Rapid Response").random()
    }
    
    val distanceKm = remember(ambulance.id, currentLocation) {
        ambulanceDistanceKm(ambulance, currentLocation)
    }

    val distanceDisplay = distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "Nearby"
    val isBusy = ambulance.status.uppercase().contains("BUSY") || ambulance.status.uppercase().contains("EMERGENCY")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ambulanceCard_${ambulance.id}")
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Color(0xFFC61111) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isBusy) Color(0xFFF5F5F5) else Color(0xFFC61111)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(type) {
                            "Basic Life Support" -> Icons.Default.MedicalServices
                            "Paramedic Rapid Response" -> Icons.Default.LocalShipping
                            else -> Icons.Default.Emergency
                        },
                        contentDescription = null,
                        tint = if (isBusy) Color.Gray else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ambulance.registrationNo.take(7),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isBusy) Color(0xFFC61111) else Color(0xFF00695C))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBusy) "BUSY (ON MISSION)" else "AVAILABLE NOW",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isBusy) Color(0xFFC61111) else Color(0xFF00695C)
                    )
                }
            }

            // Time/Distance
            Column(horizontalAlignment = Alignment.End) {
                if (isBusy) {
                    Text(text = "--", style = MaterialTheme.typography.titleLarge, color = Color.LightGray)
                } else {
                    Text(
                        text = distanceDisplay,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Text(
                        text = "DISTANCE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun ambulanceStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "AVAILABLE" -> Color(0xFF2E7D32)
        "ON_EMERGENCY", "BUSY", "ON_MISSION" -> Color(0xFFC62828)
        "OFFLINE" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
