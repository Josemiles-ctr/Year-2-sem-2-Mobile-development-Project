package com.example.mobiledev.data.location
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
class DeviceLocationProvider(
    private val context: Context
) {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }
    suspend fun getCurrentCoordinates(): Coordinates? {
        if (!hasLocationPermission()) return null
        val location = runCatching {
            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token
                )
                .await()
        }.getOrNull() ?: runCatching {
            fusedLocationClient.lastLocation.await()
        }.getOrNull()
        return location?.let { Coordinates(it.latitude, it.longitude) }
    }
    private fun hasLocationPermission(): Boolean {
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarseGranted || fineGranted
    }
}
