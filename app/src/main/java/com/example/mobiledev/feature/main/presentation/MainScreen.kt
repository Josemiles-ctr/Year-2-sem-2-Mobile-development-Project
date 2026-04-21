package com.example.mobiledev.feature.main.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobiledev.R
import com.example.mobiledev.data.mock.ActivityMetricType
import com.example.mobiledev.data.mock.MockActivityData
import com.example.mobiledev.data.model.User
import com.example.mobiledev.data.security.AppRole
import com.example.mobiledev.data.security.AuthPrincipal

private data class MainTab(
    val titleRes: Int,
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
fun MainScreen(
    currentPrincipal: AuthPrincipal,
    currentUser: User?,
    onManageStaffClick: () -> Unit = {},
    requestTabContent: @Composable () -> Unit = {},
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val tabs = listOf(
        MainTab(R.string.tab_activity, Icons.Filled.Notifications),
        MainTab(R.string.tab_requests, Icons.Filled.Warning),
        MainTab(R.string.tab_account, Icons.Filled.AccountCircle)
    )
    var selectedTabIndex by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }

    // Keep back behavior natural for tab UIs: return to default tab before exiting app.
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.titleRes)
                                )
                                Text(
                                    text = stringResource(tab.titleRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        },
                        label = null,
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        ActivitySummarySection(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    }
                    1 -> {
                        requestTabContent()
                    }
                    2 -> { // Account tab
                        AccountDetailsPanel(
                            principal = currentPrincipal,
                            currentUser = currentUser,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )

                        if (currentPrincipal.role == AppRole.HOSPITAL_ADMIN || currentPrincipal.role == AppRole.SYSTEM_ADMIN) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .clickable { onManageStaffClick() },
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null)
                                        Text("Manage Staff")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        PlaceholderScreen(
                            title = stringResource(tabs[selectedTabIndex].titleRes),
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

@Composable
private fun AccountDetailsPanel(
    principal: AuthPrincipal,
    currentUser: User?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = currentUser?.name ?: "Active User",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Role: ${principal.role.name.replace('_', ' ')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            AccountDetailRow(label = "Email", value = currentUser?.email ?: "Not available")
            AccountDetailRow(label = "Phone", value = currentUser?.phone ?: "Not available")
            AccountDetailRow(label = "User ID", value = principal.userId ?: "Not signed in")
            AccountDetailRow(label = "Hospital ID", value = principal.hospitalId ?: "N/A")
            AccountDetailRow(label = "Account Status", value = currentUser?.accountStatus ?: "Unknown")
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
        Text(
            text = "Activity Overview",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

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
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivitySummaryCard(activity: ActivitySummary) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.18f),
                shape = MaterialTheme.shapes.large
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = activity.accent.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.accent,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = activity.value,
                    style = MaterialTheme.typography.titleLarge,
                    color = activity.accent
                )
                Text(
                    text = activity.period,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun ActivityMetricType.toIcon(): ImageVector {
    return when (this) {
        ActivityMetricType.AUTH -> Icons.Default.Login
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
        ActivityMetricType.REQUEST -> Color(0xFFD32F2F)
        ActivityMetricType.ASSIGNMENT -> Color(0xFF0D47A1)
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        ElevatedCard(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = stringResource(R.string.placeholder_screen_text, title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
            )
        }
    }
}
