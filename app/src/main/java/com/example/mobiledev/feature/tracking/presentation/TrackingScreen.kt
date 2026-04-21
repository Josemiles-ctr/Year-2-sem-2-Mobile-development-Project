package com.example.mobiledev.feature.tracking.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mobiledev.BuildConfig
import com.example.mobiledev.feature.tracking.data.DirectionsApi
import com.example.mobiledev.feature.tracking.data.PolylineDecoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
    onCallClick: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ambulance Tracking",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current

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

        // Get user location
        val fusedLocationClient: FusedLocationProviderClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }

        val directionsApi = remember {
            Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DirectionsApi::class.java)
        }

        var userLocation by remember { mutableStateOf<Location?>(null) }

        LaunchedEffect(hasLocationPermission) {
            if (hasLocationPermission) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        userLocation = location
                    }
                } catch (e: SecurityException) {
                    // Handle exception if needed
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val kampala = remember { LatLng(0.3136, 32.5811) }
            val mulago = remember { LatLng(0.3476, 32.5825) }
            val nsambya = remember { LatLng(0.3031, 32.5811) }
            val ambulance1 = remember { LatLng(0.3400, 32.5750) }
            val ambulance2 = remember { LatLng(0.3550, 32.5900) }

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(kampala, 13f)
            }

            val hospitals = remember {
                listOf(
                    "Mulago Hospital" to mulago,
                    "Nsambya Hospital" to nsambya
                )
            }

            var nearestHospital by remember { mutableStateOf<Pair<String, LatLng>?>(null) }
            var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

            LaunchedEffect(userLocation) {
                userLocation?.let { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    
                    // Find nearest hospital
                    val nearest = hospitals.minByOrNull { hospital ->
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            userLatLng.latitude, userLatLng.longitude,
                            hospital.second.latitude, hospital.second.longitude,
                            results
                        )
                        results[0]
                    }
                    
                    nearestHospital = nearest
                    
                    // Update camera
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                    )

                    // Fetch actual directions from Google Maps Directions API
                    nearest?.let { hospital ->
                        try {
                            val origin = "${location.latitude},${location.longitude}"
                            val destination = "${hospital.second.latitude},${hospital.second.longitude}"
                            
                            // Use your API Key from BuildConfig (provided by secrets plugin)
                            val apiKey = BuildConfig.MAPS_API_KEY
                            
                            val response = withContext(Dispatchers.IO) {
                                directionsApi.getDirections(origin, destination, apiKey)
                            }
                            
                            if (response.routes.isNotEmpty()) {
                                val encodedPolyline = response.routes[0].overview_polyline.points
                                routePoints = PolylineDecoder.decode(encodedPolyline)
                            } else {
                                // Fallback to straight line if no route found
                                routePoints = listOf(userLatLng, hospital.second)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Fallback to straight line on error
                            routePoints = listOf(userLatLng, hospital.second)
                        }
                    }
                }
            }

            var isMapLoaded by remember { mutableStateOf(false) }

            // Map Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission
                    ),
                    onMapLoaded = { isMapLoaded = true }
                ) {
                    // Draw Route
                    if (routePoints.isNotEmpty()) {
                        Polyline(
                            points = routePoints,
                            color = MaterialTheme.colorScheme.primary,
                            width = 12f,
                            jointType = JointType.ROUND,
                            startCap = RoundCap(),
                            endCap = RoundCap()
                        )
                    }

                    // 🏥 Hospitals (RED)
                    Marker(
                        state = MarkerState(position = mulago),
                        title = "Mulago Hospital",
                        snippet = "Referral Hospital",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    Marker(
                        state = MarkerState(position = nsambya),
                        title = "Nsambya Hospital",
                        snippet = "Private Hospital",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )

                    // 🚑 Ambulances (BLUE)
                    Marker(
                        state = MarkerState(position = ambulance1),
                        title = "Ambulance MA-202",
                        snippet = "Available",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )

                    Marker(
                        state = MarkerState(position = ambulance2),
                        title = "Ambulance MA-305",
                        snippet = "En Route",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            // Bottom Info Card
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nearestHospital?.first ?: "Searching...",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = Color(0xFFE8F5E9), // Light success green
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "En Route",
                                color = Color(0xFF2E7D32), // Dark success green
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = " MA-202",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = " 1 minute",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Estimated Arrival: 1 minute",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(5.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Call")
                        }

                        Button(
                            onClick = onConfirmClick,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Confirm Request")
                        }
                    }
                }
            }
        }
    }
}
