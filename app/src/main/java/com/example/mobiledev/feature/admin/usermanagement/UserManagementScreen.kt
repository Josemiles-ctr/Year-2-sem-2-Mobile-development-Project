package com.example.mobiledev.feature.admin.usermanagement

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobiledev.core.error.toUserMessage
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.ui.components.AppBackgroundContainer
import com.example.mobiledev.ui.components.GlassyCard
import com.example.mobiledev.ui.components.AppLoadingIndicator
import com.example.mobiledev.ui.components.dialog.ConfirmationDialog
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: UserManagementViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val appError by viewModel.appError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.successMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(appError) {
        appError?.let { error ->
            snackbarHostState.showSnackbar(error.toUserMessage(context))
            viewModel.clearError()
        }
    }

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
                        title = { Text("User Management") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    ) { padding ->
        AppBackgroundContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onEvent(UserManagementEvent.SearchQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or email") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AppLoadingIndicator()
                    }
                } else {
                    val filteredUsers = state.users.filter {
                        it.name.contains(state.searchQuery, ignoreCase = true) ||
                                it.email.contains(state.searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserItem(
                                user = user,
                                onEdit = { viewModel.onEvent(UserManagementEvent.EditUser(user)) },
                                onDelete = { viewModel.onEvent(UserManagementEvent.ConfirmDeleteUser(user)) },
                                onToggleStatus = { viewModel.onEvent(UserManagementEvent.ToggleUserStatus(user)) }
                            )
                        }
                    }
                }
            }
        }

        // Edit Dialog
        state.userToEdit?.let { user ->
            EditUserDialog(
                user = user,
                onDismiss = { viewModel.onEvent(UserManagementEvent.EditUser(null)) },
                onConfirm = { updatedUser ->
                    viewModel.onEvent(UserManagementEvent.UpdateUser(updatedUser))
                }
            )
        }

        // Delete Confirmation
        state.userToDelete?.let { user ->
            ConfirmationDialog(
                title = "Delete User",
                message = "Are you sure you want to delete ${user.name}? This action cannot be undone.",
                confirmButtonText = "Delete",
                dismissButtonText = "Cancel",
                isDestructive = true,
                onConfirm = { viewModel.onEvent(UserManagementEvent.DeleteUser) },
                onDismiss = { viewModel.onEvent(UserManagementEvent.ConfirmDeleteUser(null)) }
            )
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassyCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text(user.role) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = user.accountStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.accountStatus == "ACTIVE") Color.Green else Color.Red
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            onEdit()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (user.accountStatus == "ACTIVE") "Deactivate" else "Activate") },
                        onClick = {
                            onToggleStatus()
                            showMenu = false
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var role by remember { mutableStateOf(user.role) }
    var status by remember { mutableStateOf(user.accountStatus) }
    var hospitalId by remember { mutableStateOf(user.hospitalId ?: "") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        GlassyCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 600.dp),
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit User",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    "Role", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column {
                    AppRole.entries.forEach { appRole ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = role == appRole.name,
                                onClick = { role = appRole.name }
                            )
                            Text(appRole.name, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (role == AppRole.HOSPITAL_ADMIN.name) {
                    OutlinedTextField(
                        value = hospitalId,
                        onValueChange = { hospitalId = it },
                        label = { Text("Hospital ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(
                    "Status", 
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "ACTIVE", onClick = { status = "ACTIVE" })
                    Text("ACTIVE", color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = status == "INACTIVE", onClick = { status = "INACTIVE" })
                    Text("INACTIVE", color = MaterialTheme.colorScheme.onSurface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(
                                user.copy(
                                    name = name,
                                    email = email,
                                    role = role,
                                    accountStatus = status,
                                    hospitalId = if (role == AppRole.HOSPITAL_ADMIN.name) hospitalId else null
                                )
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
