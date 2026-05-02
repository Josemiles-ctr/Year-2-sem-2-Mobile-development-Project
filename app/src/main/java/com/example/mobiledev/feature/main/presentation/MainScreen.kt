package com.example.mobiledev.feature.main.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.data.mock.ActivityMetricType
import com.example.mobiledev.data.mock.MockActivityData
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal
import com.example.mobiledev.ui.components.AppBackgroundContainer
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
    modifier: Modifier = Modifier,
    onManageStaffClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    homeTabContent: @Composable (Modifier) -> Unit = { mod ->
        ActivitySummarySection(modifier = mod)
    },
    requestTabContent: @Composable () -> Unit = {},
    userManagementTabContent: @Composable () -> Unit = {},
) {
    val firstTabTitle = if (currentPrincipal.role == AppRole.PATIENT) {
        stringResource(R.string.tab_hospitals)
    } else {
        stringResource(R.string.tab_activity)
    }
    val requestTabTitle = stringResource(R.string.tab_requests)
    val accountTabTitle = stringResource(R.string.tab_account)
    val tabs = buildList {
        add(MainTab(firstTabTitle, Icons.Filled.Notifications))
        add(MainTab(requestTabTitle, Icons.Filled.AssignmentTurnedIn))
        if (currentPrincipal.role == AppRole.SYSTEM_ADMIN) {
            add(MainTab("Users", Icons.Filled.People))
        }
        add(MainTab(accountTabTitle, Icons.Filled.Person))
    }
    val hasUserManagementTab = currentPrincipal.role == AppRole.SYSTEM_ADMIN
    val accountTabIndex = if (hasUserManagementTab) 3 else 2
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = tabs.getOrElse(selectedTabIndex) { tabs.first() }

    // Keep back behavior natural for tab UIs: return to default tab before exiting app.
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        topBar = {
            MainTopBar()
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00695C),
                                selectedTextColor = Color(0xFF00695C),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFE0F2F1)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppBackgroundContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            homeTabContent(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                        1 -> {
                            requestTabContent()
                        }
                        2 -> {
                            if (hasUserManagementTab) {
                                userManagementTabContent()
                            } else {
                                AccountDetailsPanel(
                                    principal = currentPrincipal,
                                    currentUser = currentUser,
                                    canManageStaff = currentPrincipal.role == AppRole.HOSPITAL_ADMIN ||
                                        currentPrincipal.role == AppRole.SYSTEM_ADMIN,
                                    onManageStaffClick = onManageStaffClick,
                                    onLogoutClick = onLogoutClick,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp)
                                )
                            }
                        }
                        accountTabIndex -> {
                            AccountDetailsPanel(
                                principal = currentPrincipal,
                                currentUser = currentUser,
                                canManageStaff = currentPrincipal.role == AppRole.HOSPITAL_ADMIN ||
                                    currentPrincipal.role == AppRole.SYSTEM_ADMIN,
                                onManageStaffClick = onManageStaffClick,
                                onLogoutClick = onLogoutClick,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                            )
                        }
                        else -> {
                            PlaceholderScreen(
                                title = selectedTab.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainTopBar(modifier: Modifier = Modifier) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFC61111),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ResQ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC61111)
                    )
                )
            }
        },
        actions = {
            Surface(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clickable { menuExpanded = !menuExpanded },
                shape = CircleShape,
                color = Color(0xFFF1F5F9)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.padding(6.dp),
                    tint = Color(0xFF64748B)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = { menuExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Help & Support") },
                    onClick = { menuExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("About") },
                    onClick = { menuExpanded = false }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black
        )
    )
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2F1)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF00695C),
                            modifier = Modifier
                                .padding(10.dp)
                                .size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Active User",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                        Text(
                            text = "${principal.role.name.replace('_', ' ')} account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = currentUser?.accountStatus ?: "Active",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )

                    AccountDetailRow(label = "Email", value = currentUser?.email ?: "Not available")
                    AccountDetailRow(label = "Phone", value = currentUser?.phone ?: "Not available")
                    AccountDetailRow(label = "User ID", value = principal.userId ?: "Not signed in")
                    AccountDetailRow(label = "Hospital ID", value = principal.hospitalId ?: "N/A")
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Security & Preferences",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00695C),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Two-step verification: Disabled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF00695C),
                            modifier = Modifier.size(20.dp)
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
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC61111),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActivitySummarySection(
    modifier: Modifier = Modifier
) {
    val summaries = MockActivityData.summaries.map { summary ->
        ActivitySummary(
            title = summary.title,
            description = summary.description,
            value = summary.value,
            period = summary.period,
            icon = summary.type.toIcon(),
            accent = summary.type.toAccentColor()
        )
    }

    val miniStats = MockActivityData.miniStats

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityMiniStat(
                label = miniStats[0].label,
                value = miniStats[0].value,
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )
            ActivityMiniStat(
                label = miniStats[1].label,
                value = miniStats[1].value,
                icon = Icons.Default.LocalShipping,
                modifier = Modifier.weight(1f)
            )
            ActivityMiniStat(
                label = miniStats[2].label,
                value = miniStats[2].value,
                icon = Icons.Default.Warning,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(summaries) { activity ->
                ActivitySummaryCard(activity = activity)
            }
        }
    }
}

@Composable
private fun ActivityMiniStat(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00695C),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivitySummaryCard(activity: ActivitySummary) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activity.accent.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.accent,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = activity.value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = activity.accent
                )
                Text(
                    text = activity.period,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun glassReadableAccent(color: Color): Color {
    val luminance = color.luminance()
    return when {
        luminance > 0.58f -> lerp(color, Color.Black, 0.42f)
        luminance < 0.18f -> lerp(color, Color.White, 0.18f)
        else -> color
    }
}

private fun ActivityMetricType.toIcon(): ImageVector {
    return when (this) {
        ActivityMetricType.AUTH -> Icons.AutoMirrored.Filled.Login
        ActivityMetricType.REQUEST -> Icons.Default.Warning
        ActivityMetricType.ASSIGNMENT -> Icons.Default.AssignmentTurnedIn
        ActivityMetricType.EN_ROUTE -> Icons.Default.LocalShipping
        ActivityMetricType.ARRIVAL -> Icons.Default.MedicalServices
        ActivityMetricType.COMPLETION -> Icons.Default.CheckCircle
        ActivityMetricType.STAFF -> Icons.Default.Badge
        ActivityMetricType.ACCOUNT -> Icons.Default.ManageAccounts
    }
}

private fun ActivityMetricType.toAccentColor(): Color {
    return when (this) {
        ActivityMetricType.AUTH -> Color(0xFF2E7D32)
        ActivityMetricType.REQUEST -> Color(0xFFC61111)
        ActivityMetricType.ASSIGNMENT -> Color(0xFF00695C)
        ActivityMetricType.EN_ROUTE -> Color(0xFF1565C0)
        ActivityMetricType.ARRIVAL -> Color(0xFF00838F)
        ActivityMetricType.COMPLETION -> Color(0xFF2E7D32)
        ActivityMetricType.STAFF -> Color(0xFF6A1B9A)
        ActivityMetricType.ACCOUNT -> Color(0xFF455A64)
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onTrackingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        ElevatedCard(
            modifier = Modifier.padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = stringResource(R.string.placeholder_screen_text, title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onTrackingClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                ) {
                    Text("Go to Tracking")
                }
            }
        }
    }
}
