package com.example.mobiledev.feature.triage.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    val contentColor = Color(0xFF1A1C1E) // Dark grey for readability on light backgrounds

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Triage Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Active Cases Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Active Cases",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Total: ${activeRequests.size}", 
                                    fontSize = 14.sp,
                                    color = contentColor
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Critical: ${activeRequests.count { it.priority == EmergencyPriority.CRITICAL }}", 
                                        fontSize = 14.sp,
                                        color = contentColor
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFFFA500), RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Serious: ${activeRequests.count { it.priority == EmergencyPriority.HIGH }}", 
                                        fontSize = 14.sp, 
                                        color = contentColor
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Green, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Stable: ${activeRequests.count { it.priority == EmergencyPriority.MEDIUM || it.priority == EmergencyPriority.LOW }}", 
                                        fontSize = 14.sp, 
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    PriorityTab(
                        title = "Critical (${activeRequests.count { it.priority == EmergencyPriority.CRITICAL }})",
                        isSelected = selectedPriorityTab == "Critical",
                        selectedColor = Color(0xFFD32F2F),
                        onClick = { selectedPriorityTab = "Critical" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Serious (${activeRequests.count { it.priority == EmergencyPriority.HIGH }})",
                        isSelected = selectedPriorityTab == "Serious",
                        selectedColor = Color(0xFFFFA500),
                        onClick = { selectedPriorityTab = "Serious" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Stable (${activeRequests.count { it.priority == EmergencyPriority.MEDIUM || it.priority == EmergencyPriority.LOW }})",
                        isSelected = selectedPriorityTab == "Stable",
                        selectedColor = Color(0xFF2E7D32),
                        onClick = { selectedPriorityTab = "Stable" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item {
                Text(
                    "$selectedPriorityTab Requests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            val filteredRequests = when (selectedPriorityTab) {
                "Critical" -> activeRequests.filter { it.priority == EmergencyPriority.CRITICAL }
                "Serious" -> activeRequests.filter { it.priority == EmergencyPriority.HIGH }
                "Stable" -> activeRequests.filter { it.priority == EmergencyPriority.MEDIUM || it.priority == EmergencyPriority.LOW }
                else -> activeRequests
            }
            
            items(filteredRequests) { request ->
                TriageRequestItem(
                    request = request,
                    onViewDetails = { onViewDetails(request) }
                )
            }
        }
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
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) selectedColor else Color.Transparent,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
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
    val contentColor = Color(0xFF1A1C1E)
    
    val (priorityLabel, priorityColor) = when (request.priority) {
        EmergencyPriority.CRITICAL -> "Critical" to Color.Red
        EmergencyPriority.HIGH -> "Serious" to Color(0xFFFFA500)
        EmergencyPriority.MEDIUM -> "Stable" to Color.Green
        EmergencyPriority.LOW -> "Stable" to Color.Green
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = priorityColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    priorityLabel,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(color = contentColor)) {
                    append("Patient: ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = contentColor)) {
                    append(request.patientName)
                }
            })
            
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(color = contentColor)) {
                    append("Incident: ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = contentColor)) {
                    append(request.description)
                }
            })
            
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(color = contentColor)) {
                    append("Time Elapsed: ")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = contentColor)) {
                    append(timeAgo)
                }
            })
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onViewDetails,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80DEEA)), // Cyan from screenshot
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("View Details", color = Color.Black, fontWeight = FontWeight.Bold)
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
