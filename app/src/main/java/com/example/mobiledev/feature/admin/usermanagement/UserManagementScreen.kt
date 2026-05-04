package com.example.mobiledev.feature.admin.usermanagement

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.mobiledev.ui.components.GlassyCard
import com.example.mobiledev.ui.components.AppLoadingIndicator
import com.example.mobiledev.ui.components.dialog.ConfirmationDialog
import kotlinx.coroutines.flow.collectLatest

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
        containerColor = Color(0xFFFBFBFB),
        topBar = {
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
                        text = "User Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A202C)
                    )
                    Text(
                        text = "Admin control over users and roles",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize().background(Color(0xFFFBFBFB))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize().background(Color(0xFFFBFBFB))
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Search Bar
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(UserManagementEvent.SearchQueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or email") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00695C)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF00695C),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp)
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                color = Color(0xFFF1F5F9)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEDF2F7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = user.role,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4A5568)
                        )
                    }
                    Text(
                        text = user.accountStatus,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (user.accountStatus == "ACTIVE") Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.Gray)
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
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
                    color = Color(0xFF1A202C),
                    fontWeight = FontWeight.ExtraBold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Text(
                    "Role", 
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AppRole.entries.forEach { appRole ->
                        Surface(
                            onClick = { role = appRole.name },
                            color = if (role == appRole.name) Color(0xFFE0F2F1) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = role == appRole.name,
                                    onClick = { role = appRole.name },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00695C))
                                )
                                Text(
                                    text = appRole.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (role == appRole.name) Color(0xFF00695C) else Color.Black
                                )
                            }
                        }
                    }
                }

                if (role == AppRole.HOSPITAL_ADMIN.name) {
                    OutlinedTextField(
                        value = hospitalId,
                        onValueChange = { hospitalId = it },
                        label = { Text("Hospital ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Text(
                    "Status", 
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = status == "ACTIVE", onClick = { status = "ACTIVE" })
                        Text("ACTIVE", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = status == "INACTIVE", onClick = { status = "INACTIVE" })
                        Text("INACTIVE", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00695C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
