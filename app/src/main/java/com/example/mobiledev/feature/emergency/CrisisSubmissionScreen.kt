package com.example.mobiledev.feature.emergency

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.ui.components.GlassyCard
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisSubmissionScreen(
    viewModel: CrisisSubmissionViewModel,
    onCancel: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crisis Submission", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    TextButton(onClick = onHelp) {
                        Text("Help", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Emergency Button
            Button(
                onClick = { viewModel.submitRequest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    "REQUEST EMERGENCY HELP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Incident Type Selection
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Select Incident Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IncidentTypeCard(
                        title = "Cardiac",
                        icon = Icons.Default.Favorite,
                        color = Color.Red,
                        isSelected = uiState.selectedIncidentType == "Cardiac",
                        onClick = { viewModel.onIncidentTypeSelected("Cardiac") },
                        modifier = Modifier.weight(1f)
                    )
                    IncidentTypeCard(
                        title = "Injury",
                        icon = Icons.Default.Healing,
                        color = Color.Blue,
                        isSelected = uiState.selectedIncidentType == "Injury",
                        onClick = { viewModel.onIncidentTypeSelected("Injury") },
                        modifier = Modifier.weight(1f)
                    )
                    IncidentTypeCard(
                        title = "Respiratory",
                        icon = Icons.Default.Air,
                        color = Color.Green,
                        isSelected = uiState.selectedIncidentType == "Respiratory",
                        onClick = { viewModel.onIncidentTypeSelected("Respiratory") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Incident Description
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Incident Description (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChanged(it) },
                    placeholder = { Text("Briefly describe your emergency...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val userLocation = LatLng(-1.286389, 36.817223) // Nairobi mock
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLocation, 15f)
                }
                
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = MarkerState(position = userLocation),
                        title = "Your Location",
                        snippet = uiState.address
                    )
                }

                // Address Overlay
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        uiState.address,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp
                    )
                }
            }

            // Confirm Location Button
            OutlinedButton(
                onClick = { /* Confirm location logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("Confirm Location", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (uiState.submitSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Request Sent") },
            text = { Text("Your emergency request has been sent to the nearest hospital.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState(); onCancel() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.error?.let { err ->
        LaunchedEffect(err) {
            // Show snackbar or toast
        }
    }
}

@Composable
fun IncidentTypeCard(
    title: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, color),
        color = if (isSelected) color.copy(alpha = 0.1f) else Color.White
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}
