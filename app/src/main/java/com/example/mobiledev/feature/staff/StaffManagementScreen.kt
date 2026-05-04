package com.example.mobiledev.feature.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobiledev.data.model.*

import androidx.compose.ui.res.stringResource
import com.example.mobiledev.R
import com.example.mobiledev.core.error.toUserMessage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.mobiledev.ui.components.dialog.ConfirmationDialog
import com.example.mobiledev.ui.components.GlassyCard
import com.example.mobiledev.ui.components.AppLoadingIndicator
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StaffManagementScreen(
    viewModel: StaffViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val successStaffInvited = stringResource(R.string.success_staff_invited)
    val successStaffRemoved = stringResource(R.string.success_staff_removed)
    val successInvitationCancelled = stringResource(R.string.success_invitation_cancelled)

    LaunchedEffect(Unit) {
        viewModel.successMessage.collectLatest { message ->
            val userMessage = when (message) {
                "Staff invited successfully" -> successStaffInvited
                "Staff member removed" -> successStaffRemoved
                "Invitation cancelled" -> successInvitationCancelled
                else -> message
            }
            Toast.makeText(context, userMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val appError by viewModel.appError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(appError) {
        appError?.let { error ->
            val message = error.toUserMessage(context)
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    StaffManagementContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun StaffManagementContent(
    state: StaffManagementState,
    snackbarHostState: SnackbarHostState,
    onEvent: (StaffManagementEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFFBFBFB),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Staff Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A202C)
                    )
                    Text(
                        text = "Manage hospital staff members and invitations",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(StaffManagementEvent.ToggleInviteDialog(true)) },
                containerColor = Color(0xFF00695C),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Invite Staff")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "Active Staff",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    )
                }
                
                items(state.staffList) { staff ->
                    StaffItem(
                        staff = staff,
                        onUpdateRole = { role -> onEvent(StaffManagementEvent.UpdateStaff(staff.id, role, null)) },
                        onToggleStatus = {
                            val newStatus = if (staff.status == StaffStatus.ACTIVE) StaffStatus.INACTIVE else StaffStatus.ACTIVE
                            onEvent(StaffManagementEvent.UpdateStaff(staff.id, null, newStatus))
                        },
                        onRemove = { onEvent(StaffManagementEvent.ShowRemoveStaffConfirmation(staff)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Pending Invitations",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    )
                }
                
                if (state.invitations.isEmpty()) {
                    item {
                        Text(
                            "No pending invitations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(state.invitations) { invitation ->
                        InvitationItem(
                            invitation = invitation,
                            onResend = { onEvent(StaffManagementEvent.ResendInvitation(invitation.id)) },
                            onCancel = { onEvent(StaffManagementEvent.ShowCancelInvitationConfirmation(invitation)) }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            if (state.isLoading) {
                AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (state.isInviteDialogOpen) {
                InviteStaffDialog(
                    onDismiss = { onEvent(StaffManagementEvent.ToggleInviteDialog(false)) },
                    onInvite = { email, role -> onEvent(StaffManagementEvent.InviteStaff(email, role)) }
                )
            }

            state.staffToRemove?.let { staff ->
                ConfirmationDialog(
                    title = stringResource(R.string.dialog_remove_staff_title),
                    message = stringResource(R.string.dialog_remove_staff_message, staff.name),
                    confirmButtonText = stringResource(R.string.dialog_confirm),
                    dismissButtonText = stringResource(R.string.dialog_cancel),
                    isDestructive = true,
                    onConfirm = { onEvent(StaffManagementEvent.RemoveStaff(staff.id)) },
                    onDismiss = { onEvent(StaffManagementEvent.ShowRemoveStaffConfirmation(null)) }
                )
            }

            state.invitationToCancel?.let { invitation ->
                ConfirmationDialog(
                    title = stringResource(R.string.dialog_cancel_invitation_title),
                    message = stringResource(R.string.dialog_cancel_invitation_message, invitation.email),
                    confirmButtonText = stringResource(R.string.dialog_confirm),
                    dismissButtonText = stringResource(R.string.dialog_cancel),
                    isDestructive = true,
                    onConfirm = { onEvent(StaffManagementEvent.CancelInvitation(invitation.id)) },
                    onDismiss = { onEvent(StaffManagementEvent.ShowCancelInvitationConfirmation(null)) }
                )
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = staff.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = staff.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = staff.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4A5568)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = staff.status.toString(),
                        color = if (staff.status == StaffStatus.ACTIVE) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
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
                        color = Color(0xFF00695C)
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
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invitation.email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = "Role: ${invitation.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row {
                TextButton(onClick = onResend) { 
                    Text("Resend", color = Color(0xFF00695C), fontWeight = FontWeight.Bold) 
                }
                TextButton(onClick = onCancel) { 
                    Text("Cancel", color = Color(0xFFC61111), fontWeight = FontWeight.Bold) 
                }
            }
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Invite Staff",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1A202C),
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    "Select Role",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StaffRole.entries.forEach { role ->
                        Surface(
                            onClick = { selectedRole = role },
                            color = if (selectedRole == role) Color(0xFFE0F2F1) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRole == role,
                                    onClick = { selectedRole = role },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00695C))
                                )
                                Text(
                                    text = role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedRole == role) Color(0xFF00695C) else Color.Black
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { 
                        Text("Cancel", color = Color.Gray) 
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onInvite(email, selectedRole) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00695C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { 
                        Text("Send Invitation") 
                    }
                }
            }
        }
    }
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
        StaffManagementContent(
            state = mockState,
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {}
        )
    }
}
