package com.example.mobiledev.feature.patient.presentation

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.HospitalEntity

@Composable
fun PatientHospitalDetailsRoute(
    viewModel: PatientHospitalDetailsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PatientHospitalDetailsScreen(
        viewModel = viewModel,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun PatientHospitalDetailsScreen(
    viewModel: PatientHospitalDetailsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
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
            onBackClick = onBackClick,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
        ElevatedCard(
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
    onBackClick: () -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("patientHospitalDetailsRoot"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "Hospital details",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                    DetailRow(label = "Active ambulances", value = hospital.activeAmbulances.toString())
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
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No ambulances registered for this hospital yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(ambulances, key = { it.id }) { ambulance ->
                AmbulanceCard(ambulance = ambulance)
            }
        }
    }
}

@Composable
private fun AmbulanceCard(ambulance: AmbulanceEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        )
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
                Text(text = ambulance.status, style = MaterialTheme.typography.labelMedium)
            }
            DetailRow(label = "Location", value = "${ambulance.latitude}, ${ambulance.longitude}")
        }
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
