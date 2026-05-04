package com.example.mobiledev.feature.hospital.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.model.EmergencyPriority
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.ui.components.GlassyCard

@Composable
fun PatientListScreen(
    requests: List<EmergencyRequest>,
    modifier: Modifier = Modifier,
    onPatientClick: (EmergencyRequest) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFFFBFBFB))
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
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

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(requests) { request ->
                    PatientItem(request)
                }
            }
        }
    }
}

@Composable
private fun PatientItem(request: EmergencyRequest) {
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
                color = getPriorityColor(request.priority).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = getPriorityColor(request.priority),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.patientName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = request.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            PriorityBadge(priority = request.priority)
        }
    }
}

@Composable
private fun PriorityBadge(priority: EmergencyPriority) {
    val color = getPriorityColor(priority)
    val text = priority.name

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun getPriorityColor(priority: EmergencyPriority): Color {
    return when (priority) {
        EmergencyPriority.CRITICAL -> Color(0xFFD32F2F)
        EmergencyPriority.HIGH -> Color(0xFFFFA000)
        EmergencyPriority.MEDIUM -> Color(0xFF00695C)
        EmergencyPriority.LOW -> Color(0xFF2E7D32)
    }
}
