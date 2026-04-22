package com.example.mobiledev.feature.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.model.*

@Composable
fun StaffManagementScreen(
    viewModel: StaffViewModel
) {
    val state = viewModel.uiState.collectAsState().value
    StaffManagementContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementContent(
    state: StaffManagementState,
    onEvent: (StaffManagementEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Staff Management") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(StaffManagementEvent.ToggleInviteDialog(true)) }) {
                Icon(Icons.Default.Add, contentDescription = "Invite Staff")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Active Staff", style = MaterialTheme.typography.headlineSmall)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.staffList) { staff ->
                        StaffItem(
                            staff = staff,
                            onUpdateRole = { role -> onEvent(StaffManagementEvent.UpdateStaff(staff.id, role, null)) },
                            onToggleStatus = {
                                val newStatus = if (staff.status == StaffStatus.ACTIVE) StaffStatus.INACTIVE else StaffStatus.ACTIVE
                                onEvent(StaffManagementEvent.UpdateStaff(staff.id, null, newStatus))
                            },
                            onRemove = { onEvent(StaffManagementEvent.RemoveStaff(staff.id)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Pending Invitations", style = MaterialTheme.typography.headlineSmall)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.invitations) { invitation ->
                        InvitationItem(
                            invitation = invitation,
                            onResend = { onEvent(StaffManagementEvent.ResendInvitation(invitation.id)) },
                            onCancel = { onEvent(StaffManagementEvent.CancelInvitation(invitation.id)) }
                        )
                    }
                }
            }

            if (state.isInviteDialogOpen) {
                InviteStaffDialog(
                    onDismiss = { onEvent(StaffManagementEvent.ToggleInviteDialog(false)) },
                    onInvite = { email, role -> onEvent(StaffManagementEvent.InviteStaff(email, role)) }
                )
            }

            state.error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text(it) }
            }
        }
    }
}

@Composable
fun StaffItem(
    staff: StaffMember,
    onUpdateRole: (StaffRole) -> Unit,
    onToggleStatus: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(staff.name, fontWeight = FontWeight.Bold)
                Text(staff.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Role: ${staff.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Status: ${staff.status}",
                    color = if (staff.status == StaffStatus.ACTIVE) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (staff.status == StaffStatus.ACTIVE) "Deactivate" else "Reactivate") },
                        onClick = { onToggleStatus(); showMenu = false }
                    )
                    
                    HorizontalDivider()
                    Text(
                        "Change Role",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StaffRole.entries.forEach { role ->
                        if (role != staff.role) {
                            DropdownMenuItem(
                                text = { Text("Set as ${role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}") },
                                onClick = { 
                                    onUpdateRole(role)
                                    showMenu = false 
                                }
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Remove", color = Color.Red) },
                        onClick = { onRemove(); showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun InvitationItem(
    invitation: StaffInvitation,
    onResend: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invitation.email, fontWeight = FontWeight.Bold)
                Text(
                    text = "Role: ${invitation.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Expires: 7 days", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onResend) { Text("Resend") }
            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("Cancel") }
        }
    }
}

@Composable
fun InviteStaffDialog(
    onDismiss: () -> Unit,
    onInvite: (String, StaffRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(StaffRole.REQUEST_REVIEWER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Staff") },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Select Role:")
                StaffRole.entries.forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                        Text(role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onInvite(email, selectedRole) }) { Text("Invite") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StaffManagementPreview() {
    val mockState = StaffManagementState(
        staffList = listOf(
            StaffMember("1", "John Doe", "john@example.com", StaffRole.FULL_ADMIN, StaffStatus.ACTIVE),
            StaffMember("2", "Jane Smith", "jane@example.com", StaffRole.AMBULANCE_COORDINATOR, StaffStatus.INACTIVE)
        ),
        invitations = listOf(
            StaffInvitation("1", "pending@example.com", StaffRole.REQUEST_REVIEWER, System.currentTimeMillis(), System.currentTimeMillis() + 604800000)
        )
    )
    MaterialTheme {
        StaffManagementContent(state = mockState, onEvent = {})
    }
}
