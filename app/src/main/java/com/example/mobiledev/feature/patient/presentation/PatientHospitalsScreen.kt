package com.example.mobiledev.feature.patient.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.example.mobiledev.data.location.Coordinates
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.ui.components.FullScreenLoading
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay
import java.util.Locale

private const val PAGE_SIZE = 6

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PatientHospitalsScreen(
    viewModel: PatientHospitalsViewModel,
    modifier: Modifier = Modifier,
    onHospitalClick: (String) -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    currentLocation: Coordinates? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchText by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var visibleCount by rememberSaveable { mutableIntStateOf(PAGE_SIZE) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchText) {
        delay(300)
        debouncedQuery = searchText
    }

    LaunchedEffect(debouncedQuery, currentLocation) {
        visibleCount = PAGE_SIZE
    }

    val orderedHospitals = remember(uiState.hospitals, currentLocation) {
        sortHospitalsByDistance(uiState.hospitals, currentLocation)
    }

    val filteredHospitals = remember(orderedHospitals, debouncedQuery) {
        filterHospitalsByQuery(orderedHospitals, debouncedQuery)
    }

    val visibleHospitals = remember(filteredHospitals, visibleCount) {
        filteredHospitals.take(visibleCount)
    }

    LaunchedEffect(listState, visibleHospitals.size, filteredHospitals.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collectLatest { lastVisibleIndex ->
                if (visibleHospitals.isNotEmpty() &&
                    lastVisibleIndex >= visibleHospitals.lastIndex - 1 &&
                    visibleCount < filteredHospitals.size
                ) {
                    visibleCount = (visibleCount + PAGE_SIZE).coerceAtMost(filteredHospitals.size)
                }
            }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBFBFB))
            .pullRefresh(pullRefreshState)
            .testTag("patientHospitalsRoot")
    ) {
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::refresh
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Header Section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Find Emergency Care",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A202C)
                            )
                        )
                        Text(
                            text = "Locate the nearest medical facility",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Search Bar
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("hospitalSearchField"),
                        placeholder = {
                            Text(
                                "Search by hospital or trauma level...",
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF00695C)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onRefreshLocation) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Refresh Location",
                                    tint = if (currentLocation != null) Color(0xFF00695C) else Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFF00695C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recommended for You",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D3748)
                                )
                            )
                        }
                        Surface(
                            color = Color(0xFFEDF2F7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${filteredHospitals.size} Found",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF4A5568),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when {
                        filteredHospitals.isEmpty() -> EmptyState()
                        else -> LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(visibleHospitals, key = { it.id }) { hospital ->
                                HospitalCard(
                                    hospital = hospital,
                                    distanceKm = hospitalDistanceKm(hospital, currentLocation),
                                    onClick = { onHospitalClick(hospital.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        PullRefreshIndicator(
            refreshing = uiState.isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFBFBFB)),
        contentAlignment = Alignment.Center
    ) {
        FullScreenLoading()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFC61111),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Unable to load hospitals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = Color(0xFF00695C),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No hospitals match your search",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "Try a different hospital name, phone number, or address.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HospitalCard(
    hospital: HospitalEntity,
    distanceKm: Double? = null,
    onClick: () -> Unit
) {
    val distanceDisplay = distanceKm?.let { String.format(Locale.US, "%.1f km", it) } ?: "Nearby"
    val traumaLevel = remember(hospital.id) { listOf("Level I", "Level II", "Level III").random() }
    val isBusy = hospital.activeAmbulances < 2

    val statusText = if (isBusy) "BUSY" else "AVAILABLE"
    val statusColor = if (isBusy) Color(0xFFC62828) else Color(0xFF2E7D32)
    val statusBgColor = if (isBusy) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hospitalCard_${hospital.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Status, Distance, AMBS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = statusBgColor,
                        shape = CircleShape
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = statusColor
                            )
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${hospital.activeAmbulances}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBusy) Color(0xFFC62828) else Color(0xFF00695C)
                            )
                        )
                        Text(
                            text = "AMBS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }

            // Hospital Name
            Text(
                text = hospital.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C),
                    letterSpacing = (-0.5).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Distance and Trauma Level Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            "Distance",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = distanceDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00695C)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Trauma Level",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = traumaLevel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3748)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00695C),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "View Ambulances",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}



