package com.example.mobiledev.feature.triage.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.data.model.EmergencyPriority
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus
import com.example.mobiledev.feature.emergency.EmergencyViewModel
import java.util.concurrent.TimeUnit

@Composable
fun TriageDashboardScreen(
    viewModel: EmergencyViewModel,
    onViewDetails: (EmergencyRequest) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeRequests = uiState.requests.filter { 
        it.status != EmergencyStatus.COMPLETED && it.status != EmergencyStatus.CANCELLED 
    }
    var selectedPriorityTab by remember { mutableStateOf("Critical") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB))
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

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Active Cases Summary Card
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Active Cases Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00695C)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SummaryItem(
                                label = "Total",
                                value = "${activeRequests.size}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryItem(
                                label = "Critical",
                                value = "${activeRequests.count { it.priority == EmergencyPriority.CRITICAL }}",
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryItem(
                                label = "Serious",
                                value = "${activeRequests.count { it.priority == EmergencyPriority.HIGH }}",
                                color = Color(0xFFFFA500),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryItem(
                                label = "Stable",
                                value = "${activeRequests.count { it.priority == EmergencyPriority.MEDIUM || it.priority == EmergencyPriority.LOW }}",
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Priority Tab Selector
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                ) {
                    PriorityTab(
                        title = "Critical",
                        isSelected = selectedPriorityTab == "Critical",
                        selectedColor = Color(0xFFD32F2F),
                        onClick = { selectedPriorityTab = "Critical" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Serious",
                        isSelected = selectedPriorityTab == "Serious",
                        selectedColor = Color(0xFFFFA500),
                        onClick = { selectedPriorityTab = "Serious" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Stable",
                        isSelected = selectedPriorityTab == "Stable",
                        selectedColor = Color(0xFF2E7D32),
                        onClick = { selectedPriorityTab = "Stable" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            val filteredRequests = when (selectedPriorityTab) {
                "Critical" -> activeRequests.filter { it.priority == EmergencyPriority.CRITICAL }
                "Serious" -> activeRequests.filter { it.priority == EmergencyPriority.HIGH }
                "Stable" -> activeRequests.filter { it.priority == EmergencyPriority.MEDIUM || it.priority == EmergencyPriority.LOW }
                else -> activeRequests
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                items(filteredRequests) { request ->
                    TriageRequestItem(
                        request = request,
                        onViewDetails = { onViewDetails(request) }
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
fun PriorityTab(
    title: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) selectedColor else Color.Transparent,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun TriageRequestItem(
    request: EmergencyRequest,
    onViewDetails: () -> Unit
) {
    val timeAgo = getTimeAgo(request.timestamp)
    
    val (priorityLabel, priorityColor) = when (request.priority) {
        EmergencyPriority.CRITICAL -> "Critical" to Color(0xFFD32F2F)
        EmergencyPriority.HIGH -> "Serious" to Color(0xFFFFA500)
        EmergencyPriority.MEDIUM -> "Stable" to Color(0xFF2E7D32)
        EmergencyPriority.LOW -> "Stable" to Color(0xFF2E7D32)
    }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                        priorityLabel,
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
                text = request.patientName,
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
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, 
                    contentColor = Color(0xFF1A202C)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text("View Details", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    return when {
        minutes < 1 -> "Just now"
        else -> "${minutes} min ago"
    }
}
