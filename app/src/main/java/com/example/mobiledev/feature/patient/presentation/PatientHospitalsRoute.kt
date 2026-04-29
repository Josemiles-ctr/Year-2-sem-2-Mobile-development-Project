package com.example.mobiledev.feature.patient.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.mobiledev.data.location.Coordinates
import com.example.mobiledev.data.location.DeviceLocationProvider

@Composable
fun PatientHospitalsRoute(
    viewModel: PatientHospitalsViewModel,
    modifier: Modifier = Modifier,
    onHospitalClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val locationProvider = remember(context) { DeviceLocationProvider(context) }
    var currentLocation by remember { mutableStateOf<Coordinates?>(null) }
    var permissionRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            permissionRequested = true
        }
    }

    LaunchedEffect(permissionRequested) {
        if (permissionRequested) {
            currentLocation = locationProvider.getCurrentCoordinates()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            permissionRequested = true
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    PatientHospitalsScreen(
        viewModel = viewModel,
        onHospitalClick = onHospitalClick,
        currentLocation = currentLocation,
        modifier = modifier
    )
}

