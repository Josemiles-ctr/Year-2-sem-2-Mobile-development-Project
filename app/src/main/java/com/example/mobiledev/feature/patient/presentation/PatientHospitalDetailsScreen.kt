package com.example.mobiledev.feature.patient.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onBackClick: () -> Unit,
    onAmbulanceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationProvider = remember(context) { DeviceLocationProvider(context) }
    var currentLocation by remember { mutableStateOf<Coordinates?>(null) }
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
            currentLocation = locationProvider.getCurrentCoordinates()
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

    when {
        uiState.isLoading -> LoadingState(modifier)
        uiState.errorMessage != null -> ErrorState(
            message = uiState.errorMessage.orEmpty(),
            onBackClick = onBackClick,
            modifier = modifier
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
            onSubmitEmergencyRequest = { desc ->
                viewModel.submitEmergencyRequest(
                    desc,
                    currentLocation?.latitude,
                    currentLocation?.longitude
                )
            },
            onDismissSubmitMessage = viewModel::clearSubmitMessage,
            onBackClick = onBackClick,
            onAmbulanceClick = onAmbulanceClick,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    FullScreenLoading(modifier = modifier)
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
                    Text("Back")
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
    onSubmitEmergencyRequest: (String) -> Unit,
    onDismissSubmitMessage: () -> Unit,
    onBackClick: () -> Unit,
    onAmbulanceClick: (String) -> Unit,
    modifier: Modifier
) {
    var showRequestDialog by rememberSaveable { mutableStateOf(false) }
    var emergencyDescription by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("patientHospitalDetailsRoot"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassyCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Hospital details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(hospital.name, style = MaterialTheme.typography.titleMedium)
                            Text("Approved hospital", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    DetailRow(label = "Phone", value = hospital.phone)
                    DetailRow(label = "Address", value = hospital.location)
                    val distanceKm = hospitalDistanceKm(hospital, currentLocation)
                    DetailRow(
                        label = "Distance",
                        value = distanceKm?.let {
                            String.format(Locale.getDefault(), "%.1f km", it)
                        } ?: "Distance unavailable"
                    )
                    DetailRow(label = "Available ambulances", value = availableAmbulanceCount.toString())

                    Button(
                        onClick = {
                            emergencyDescription = ""
                            showRequestDialog = true
                        },
                        enabled = !isHospitalOffline && !isSubmittingRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("requestEmergencyButton")
                    ) {
                        Text(if (isSubmittingRequest) "Submitting..." else "Request emergency")
                    }
                }
            }
        }

        if (submitSuccessMessage != null) {
            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = submitSuccessMessage,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onDismissSubmitMessage) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        if (submitErrorMessage != null) {
            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = submitErrorMessage,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = onDismissSubmitMessage) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        if (isHospitalOffline) {
            item {
                GlassyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offlineHospitalMessage"),
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "This hospital is currently offline. No active ambulances are available.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Text(
                text = "Ambulances",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (ambulances.isEmpty()) {
            item {
                GlassyCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No ambulances registered for this hospital yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(ambulances, key = { it.id }) { ambulance ->
                AmbulanceCard(
                    ambulance = ambulance,
                    onClick = { onAmbulanceClick(ambulance.id) }
                )
            }
        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = {
                showRequestDialog = false
            },
            title = { Text("Submit emergency request") },
            text = {
                OutlinedTextField(
                    value = emergencyDescription,
                    onValueChange = { emergencyDescription = it },
                    label = { Text("Emergency details") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitEmergencyRequest(emergencyDescription)
                        showRequestDialog = false
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRequestDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = {
                showRequestDialog = false
            },
            title = { Text("Submit emergency request") },
            text = {
                OutlinedTextField(
                    value = emergencyDescription,
                    onValueChange = { emergencyDescription = it },
                    label = { Text("Emergency details") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitEmergencyRequest(emergencyDescription)
                        showRequestDialog = false
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRequestDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AmbulanceCard(
    ambulance: AmbulanceEntity,
    onClick: () -> Unit
) {
    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ambulance.registrationNo,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = ambulance.licenseNo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = ambulance.status.replace('_', ' '),
                    style = MaterialTheme.typography.labelMedium,
                    color = ambulanceStatusColor(ambulance.status),
                    modifier = Modifier.testTag("ambulanceStatus_${ambulance.id}")
                )
            }
            DetailRow(label = "Location", value = "${ambulance.latitude}, ${ambulance.longitude}")
        }
    }
}

@Composable
private fun ambulanceStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "AVAILABLE" -> Color(0xFF4CAF50)
        "ON_EMERGENCY", "BUSY", "ON_MISSION" -> Color(0xFFF44336)
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
