package com.example.mobiledev.feature.staff

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

import androidx.compose.ui.res.stringResource
import com.example.mobiledev.R
import com.example.mobiledev.core.error.AppError
import com.example.mobiledev.core.error.toUserMessage
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.mobiledev.ui.components.dialog.ConfirmationDialog
import com.example.mobiledev.ui.components.AppBackgroundContainer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementContent(
    state: StaffManagementState,
    snackbarHostState: SnackbarHostState,
    onEvent: (StaffManagementEvent) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
                ) {
                    TopAppBar(
                        title = { Text("Staff Management") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(StaffManagementEvent.ToggleInviteDialog(true)) }) {
                Icon(Icons.Default.Add, contentDescription = "Invite Staff")
            }
        }
    ) { padding ->
        AppBackgroundContainer(modifier = Modifier.padding(padding)) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
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
                                onRemove = { onEvent(StaffManagementEvent.ShowRemoveStaffConfirmation(staff)) }
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
                                onCancel = { onEvent(StaffManagementEvent.ShowCancelInvitationConfirmation(invitation)) }
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

    GlassyCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
    GlassyCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Invite Staff",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Select Role:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column {
                    StaffRole.entries.forEach { role ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedRole == role, onClick = { selectedRole = role })
                            Text(
                                role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onInvite(email, selectedRole) }) { Text("Invite") }
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
