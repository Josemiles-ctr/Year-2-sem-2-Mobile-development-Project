package com.example.mobiledev.feature.triage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus
import com.example.mobiledev.feature.emergency.EmergencyDashboardState
import com.example.mobiledev.feature.emergency.EmergencyViewModel
import com.example.mobiledev.ui.components.AppBackgroundContainer
import java.util.concurrent.TimeUnit

@Composable
fun TriageDashboardScreen(
    viewModel: EmergencyViewModel,
    onViewDetails: (EmergencyRequest) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPriorityTab by remember { mutableStateOf("Critical") }

    Scaffold(
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
                                Text("Total: ${uiState.requests.size}", fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Critical: ${uiState.requests.count { it.status == EmergencyStatus.PENDING }}", fontSize = 14.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Orange, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Serious: 15", fontSize = 14.sp) // Mock for now
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(12.dp).background(Color.Green, RoundedCornerShape(2.dp)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stable: 22", fontSize = 14.sp) // Mock for now
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
                        title = "Critical (${uiState.requests.count { it.status == EmergencyStatus.PENDING }})",
                        isSelected = selectedPriorityTab == "Critical",
                        selectedColor = Color(0xFFD32F2F),
                        onClick = { selectedPriorityTab = "Critical" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Serious (15)",
                        isSelected = selectedPriorityTab == "Serious",
                        selectedColor = Color(0xFFEEEEEE),
                        onClick = { selectedPriorityTab = "Serious" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityTab(
                        title = "Stable (22)",
                        isSelected = selectedPriorityTab == "Stable",
                        selectedColor = Color(0xFFEEEEEE),
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
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            val filteredRequests = when (selectedPriorityTab) {
                "Critical" -> uiState.requests.filter { it.status == EmergencyStatus.PENDING }
                else -> emptyList() // Other mocks
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = Color.Red,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "Critical",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(buildAnnotatedString {
                append("Patient: ")
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(request.patientName)
                }
            })
            
            Text(buildAnnotatedString {
                append("Incident: ")
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(request.description)
                }
            })
            
            Text(buildAnnotatedString {
                append("Time Elapsed: ")
                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(timeAgo)
                }
            })
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onViewDetails,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("View Details")
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

private fun buildAnnotatedString(block: androidx.compose.ui.text.AnnotatedString.Builder.() -> Unit) =
    androidx.compose.ui.text.AnnotatedString.Builder().apply(block).toAnnotatedString()

private fun withStyle(style: androidx.compose.ui.text.SpanStyle, block: () -> Unit) {
    // This is just a helper, actually we use AnnotatedString.Builder.withStyle
}
