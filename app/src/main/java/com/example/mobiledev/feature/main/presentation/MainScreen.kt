package com.example.mobiledev.feature.main.presentation

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobiledev.R
import com.example.mobiledev.data.mock.ActivityMetricType
import com.example.mobiledev.data.mock.MockActivityData
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.ui.components.GlassyCard

private data class MainTab(
    val title: String,
    val icon: ImageVector
)

private data class ActivitySummary(
    val title: String,
    val description: String,
    val value: String,
    val period: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(
    currentPrincipal: AuthPrincipal,
    currentUser: User?,
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onManageStaffClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    emergencyTabContent: @Composable () -> Unit = {},
    triageTabContent: @Composable () -> Unit = {},
    driversTabContent: @Composable (Modifier) -> Unit = {},
    patientsTabContent: @Composable (Modifier) -> Unit = {},
    reportsTabContent: @Composable (Modifier) -> Unit = {},
    hospitalsTabContent: @Composable (Modifier) -> Unit = {},
    notificationsTabContent: @Composable () -> Unit = {},
    profileTabContent: @Composable (Modifier) -> Unit = {},
    requestTabContent: @Composable () -> Unit = {},
    userManagementTabContent: @Composable () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val tabs = buildList {
        if (currentPrincipal.role == AppRole.PATIENT) {
            add(MainTab("Emergency", Icons.Default.NotificationsActive))
            add(MainTab("Hospitals", Icons.Default.LocalHospital))
            add(MainTab("Notifications", Icons.Default.Notifications))
            add(MainTab("Profile", Icons.Default.Person))
        } else {
            add(MainTab("Triage", Icons.Default.Assignment))
            add(MainTab("Drivers", Icons.Default.LocalShipping))
            add(MainTab("Patients", Icons.Default.People))
            add(MainTab("Reports", Icons.Default.BarChart))
            add(MainTab("Profile", Icons.Default.Person))
        }
    }
    
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = tabs.getOrElse(selectedTabIndex) { tabs.first() }

    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0

    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFFFBFBFB),
        topBar = {
            if (currentPrincipal.role != AppRole.PATIENT || selectedTabIndex != 0) {
                MainTopBar(
                    unreadCount = unreadNotificationsCount,
                    onNotificationsClick = onNotificationsClick
                )
            }
        },
        bottomBar = {
            if (!isKeyboardVisible) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                if (tab.title == "Notifications") {
                                    BadgedBox(
                                        badge = {
                                            if (unreadNotificationsCount > 0) {
                                                Badge {
                                                    Text(text = unreadNotificationsCount.toString())
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFC61111),
                                selectedTextColor = Color(0xFFC61111),
                                indicatorColor = Color(0xFFF1F5F9),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                )
        ) {
            when (selectedTabIndex) {
                0 -> {
                    if (currentPrincipal.role == AppRole.PATIENT) {
                        emergencyTabContent()
                    } else {
                        triageTabContent()
                    }
                }
                1 -> {
                    if (currentPrincipal.role == AppRole.PATIENT) {
                        hospitalsTabContent(Modifier.fillMaxSize())
                    } else {
                        driversTabContent(Modifier.fillMaxSize())
                    }
                }
                2 -> {
                    if (currentPrincipal.role == AppRole.PATIENT) {
                        notificationsTabContent()
                    } else {
                        patientsTabContent(Modifier.fillMaxSize())
                    }
                }
                3 -> {
                    if (currentPrincipal.role == AppRole.PATIENT) {
                         AccountDetailsPanel(
                            principal = currentPrincipal,
                            currentUser = currentUser,
                            canManageStaff = false,
                            onManageStaffClick = {},
                            onLogoutClick = onLogoutClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                         reportsTabContent(Modifier.fillMaxSize())
                    }
                }
                4 -> {
                    AccountDetailsPanel(
                        principal = currentPrincipal,
                        currentUser = currentUser,
                        canManageStaff = currentPrincipal.role == AppRole.HOSPITAL_ADMIN ||
                                currentPrincipal.role == AppRole.SYSTEM_ADMIN,
                        onManageStaffClick = onManageStaffClick,
                        onLogoutClick = onLogoutClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    PlaceholderScreen(
                        title = selectedTab.title,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTopBar(
    unreadCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFC61111)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(4.dp).size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ResQ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC61111),
                    letterSpacing = 0.5.sp
                )
            }

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge {
                                Text(text = unreadCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountDetailsPanel(
    principal: AuthPrincipal,
    currentUser: User?,
    canManageStaff: Boolean,
    onManageStaffClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column {
                Text(
                    text = "My Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = "Manage your profile and security settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF1A202C),
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Active User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${principal.role.name.replace('_', ' ')} account",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1A202C),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF2E7D32))
                        ) {
                            Text(
                                text = currentUser?.accountStatus ?: "ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Session Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )

                        AccountDetailRow(label = "Email", value = currentUser?.email ?: "Not available")
                        AccountDetailRow(label = "Phone", value = currentUser?.phone ?: "Not available")
                        AccountDetailRow(label = "User ID", value = principal.userId ?: "Not signed in")
                        AccountDetailRow(label = "Hospital ID", value = principal.hospitalId ?: "N/A")
                    }
                }
            }

            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Security & Preferences",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF1A202C),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Two-step verification: Disabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF1A202C),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Alert notifications: Enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            item {
                GlassyCard(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quick Actions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )

                        if (canManageStaff) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onManageStaffClick() },
                                shape = MaterialTheme.shapes.medium,
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF1A202C))
                                    Text("Manage Staff", fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color(0xFF666666))
                                Text("Profile update tools coming soon", color = Color(0xFF666666))
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFFC61111)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC61111)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Logout from Session", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onTrackingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This section is under development",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.LightGray
        )
    }
}
