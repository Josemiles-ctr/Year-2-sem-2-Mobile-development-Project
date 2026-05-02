package com.example.mobiledev.feature.tracking.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale
import com.example.mobiledev.R
import com.example.mobiledev.feature.patient.presentation.haversineKm
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocalHospital

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTrackingFinished: () -> Unit = {},
    onCallClick: () -> Unit = {},
    viewModel: TrackingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.3136, 32.5811), 13f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val fusedLocationClient: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationProvider = remember(context) { com.example.mobiledev.data.location.DeviceLocationProvider(context) }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            onTrackingFinished()
        }
    }

    fun handleCall() {
        uiState.nearestHospital?.phone?.let { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val coords = locationProvider.getCurrentCoordinates()
            coords?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                viewModel.updateUserLocation(userLatLng, context)
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            }
        }
    }

    LaunchedEffect(uiState.nearestAmbulance, uiState.requestSent) {
        if (uiState.requestSent && uiState.nearestAmbulance != null && uiState.userLocation != null) {
            val ambulanceLoc = LatLng(uiState.nearestAmbulance!!.latitude, uiState.nearestAmbulance!!.longitude)
            val userLoc = uiState.userLocation!!
            
            val bounds = LatLngBounds.builder()
                .include(ambulanceLoc)
                .include(userLoc)
                .build()
            
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 200),
                1000
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
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
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Gray
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp),
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {
                // Draw Route
                if (uiState.routePoints.isNotEmpty()) {
                    Polyline(
                        points = uiState.routePoints,
                        color = Color(0xFFC61111),
                        width = 8f,
                        pattern = listOf(Dash(20f), Gap(20f)),
                        jointType = JointType.ROUND
                    )
                }

                // Ambulances
                uiState.ambulances.forEach { ambulance ->
                    val trackedAmbulance = uiState.nearestAmbulance
                    val isTracked = ambulance.id == trackedAmbulance?.id
                    val position = if (isTracked && trackedAmbulance != null) {
                        LatLng(trackedAmbulance.latitude, trackedAmbulance.longitude)
                    } else {
                        LatLng(ambulance.latitude, ambulance.longitude)
                    }
                    Marker(
                        state = MarkerState(position = position),
                        title = "Ambulance ${ambulance.registrationNo}",
                        icon = if (isTracked) {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        } else {
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        },
                        alpha = if (isTracked) 1.0f else 0.5f
                    )
                }

                // User Location / Pickup Point Marker
                uiState.userLocation?.let { userLoc ->
                    Marker(
                        state = MarkerState(position = userLoc),
                        title = "Your Location",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
            }

            // Pickup Point Card
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFC61111)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "PICKUP POINT",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = uiState.userAddress ?: "Identifying location...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Black
                        )
                    }
                }
            }


            // Bottom UI
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (uiState.requestSent) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        AmbulanceArrivingOverlay(
                            ambulanceRegNo = uiState.nearestAmbulance?.registrationNo ?: "AMB-9921",
                            distanceKm = uiState.distanceKm,
                            onCancelClick = { viewModel.onCancelRequest() },
                            onPairClick = { viewModel.onPairRequest() }
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column {
                            // Availability Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00695C))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE AVAILABILITY",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF00695C)
                                    )
                                }
                                Text(
                                    text = "ID: ${uiState.nearestAmbulance?.registrationNo?.take(8) ?: "AMB-9921"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Ambulance Image Placeholder
                                    Surface(
                                        modifier = Modifier.size(64.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFEBEE)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            modifier = Modifier.padding(12.dp),
                                            tint = Color(0xFFC61111)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = uiState.nearestAmbulance?.registrationNo ?: "Ambulance",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = (-1).sp
                                            ),
                                            color = Color.Black
                                        )
                                        Text(
                                            text = uiState.nearestHospital?.name ?: "En route",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = uiState.nearestAmbulance?.let { ambulance ->
                                                uiState.userLocation?.let { userLoc ->
                                                    val results = FloatArray(1)
                                                    android.location.Location.distanceBetween(
                                                        userLoc.latitude, userLoc.longitude,
                                                        ambulance.latitude, ambulance.longitude,
                                                        results
                                                    )
                                                    String.format(Locale.US, "%.1f km", results[0] / 1000.0)
                                                } ?: "Nearby"
                                            } ?: "Nearby",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC61111)
                                            )
                                        )
                                        Text(
                                            text = "APPROX\nDISTANCE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.End,
                                            lineHeight = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { handleCall() },
                                        modifier = Modifier.size(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.Gray)
                                    }

                                    Button(
                                        onClick = { viewModel.onConfirmRequest() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFC61111),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = "Confirm Request",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFC61111))
                }
            }

            if (uiState.showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.onDismissDialog() },
                    title = {
                        Text(
                            "Confirm Emergency Request",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to request an ambulance to your current location? This will alert emergency services immediately.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.onConfirmDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC61111),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirm Request", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onDismissDialog() }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun AmbulanceArrivingOverlay(
    ambulanceRegNo: String,
    distanceKm: Double?,
    onCancelClick: () -> Unit,
    onPairClick: () -> Unit
) {
    var pairButtonEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(distanceKm) {
        if (distanceKm != null && distanceKm < 0.05) { // 50 meters
            pairButtonEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFC61111).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFFC61111),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = distanceKm?.let {
                        if (it < 0.1) "Ambulance arrived" else "Ambulance on the way"
                    } ?: "Ambulance on the way",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "Reg No: $ambulanceRegNo • ${distanceKm?.let { String.format(Locale.US, "%.1f km away", it) } ?: "En route"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Color(0xFFC61111),
            trackColor = Color(0xFFC61111).copy(alpha = 0.1f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = onPairClick,
                enabled = pairButtonEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC61111),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFC61111).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    "Pair Device",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pair with ambulance IoT device on arrival",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
