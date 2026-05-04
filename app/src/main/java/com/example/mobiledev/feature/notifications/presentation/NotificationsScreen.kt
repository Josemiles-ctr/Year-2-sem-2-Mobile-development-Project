package com.example.mobiledev.feature.notifications.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.local.entity.NotificationEntity
import com.example.mobiledev.ui.components.GlassyCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNotification by remember { mutableStateOf<NotificationEntity?>(null) }

    if (selectedNotification != null) {
        NotificationDetailDialog(
            notification = selectedNotification!!,
            onDismiss = { selectedNotification = null }
        )
    }

    Scaffold(
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
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )
                        Text(
                            text = "Stay updated with emergency alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.notifications.isEmpty()) {
                    item {
                        GlassyCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.White
                        ) {
                            Text(
                                text = "No notifications yet.",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(uiState.notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                selectedNotification = notification
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun getNotificationIconAndColor(type: String): Pair<ImageVector, Color> {
    return when (type) {
        "AMBULANCE_ASSIGNED" -> Icons.Default.LocalShipping to Color(0xFF2E7D32)
        "EMERGENCY_REQUEST" -> Icons.Default.Emergency to Color(0xFFC61111)
        "STATUS_CHANGE" -> Icons.Default.Info to Color(0xFF1976D2)
        "SYSTEM_ALERT" -> Icons.Default.Settings to Color(0xFF7B1FA2)
        else -> Icons.Default.Notifications to Color.Gray
    }
}

@Composable
private fun NotificationDetailDialog(
    notification: NotificationEntity,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(notification.timestamp))
    val (icon, color) = getNotificationIconAndColor(notification.type)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFC61111), fontWeight = FontWeight.Bold)
            }
        },
        icon = {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = timeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@Composable
private fun NotificationItem(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val timeString = dateFormat.format(Date(notification.timestamp))
    val (icon, color) = getNotificationIconAndColor(notification.type)

    GlassyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (notification.isRead) Color(0xFFF1F5F9) else color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (notification.isRead) Color.Gray else color,
                    modifier = Modifier.padding(10.dp).size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Bold else FontWeight.ExtraBold,
                        color = Color(0xFF1A202C)
                    )
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notification.isRead) Color.Gray else Color.DarkGray,
                    maxLines = 2
                )
            }
            
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color(0xFFC61111)
                ) {}
            }
        }
    }
}
