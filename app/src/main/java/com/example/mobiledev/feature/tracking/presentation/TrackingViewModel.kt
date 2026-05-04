package com.example.mobiledev.feature.tracking.presentation

import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mobiledev.BuildConfig
import com.example.mobiledev.ResQApplication
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.repository.ResQRepository
import com.example.mobiledev.feature.tracking.data.DirectionsApi
import com.example.mobiledev.feature.tracking.data.PolylineDecoder
import com.example.mobiledev.feature.patient.presentation.haversineKm
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class TrackingUiState(
    val userLocation: LatLng? = null,
    val hospitals: List<HospitalEntity> = emptyList(),
    val ambulances: List<AmbulanceEntity> = emptyList(),
    val nearestHospital: HospitalEntity? = null,
    val nearestAmbulance: AmbulanceEntity? = null,
    val selectedAmbulance: AmbulanceEntity? = null,
    val routePoints: List<LatLng> = emptyList(),
    val userAddress: String? = null,
    val requestSent: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val isFinished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val distanceKm: Double? = null,
    val traumaLevel: String = "Level I"
)

class TrackingViewModel(
    private val repository: ResQRepository,
    private val initialAmbulanceId: String? = null
) : ViewModel() {

    class TrackingViewModelFactory(
        private val repository: ResQRepository,
        private val ambulanceId: String? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackingViewModel(repository, ambulanceId) as T
        }
    }

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private val directionsApi = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DirectionsApi::class.java)

    private var hospitalsJob: Job? = null
    private var ambulancesJob: Job? = null
    private var simulationJob: Job? = null

    init {
        observeTrackingData()
    }

    private fun observeTrackingData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Hospitals
            hospitalsJob?.cancel()
            hospitalsJob = launch {
                try {
                    repository.getAllHospitalsStream().collect { hospitals ->
                        _uiState.update { it.copy(hospitals = hospitals) }
                        uiState.value.userLocation?.let { findNearestEntities(it) }
                    }
                } catch (e: SecurityException) {
                    try {
                        repository.getApprovedHospitalsStream().collect { hospitals ->
                            _uiState.update { it.copy(hospitals = hospitals) }
                            uiState.value.userLocation?.let { findNearestEntities(it) }
                        }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(error = e.message) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }

            // Ambulances
            ambulancesJob?.cancel()
            ambulancesJob = launch {
                try {
                    repository.getAllAmbulancesStream().collect { ambulances ->
                        if (!_uiState.value.requestSent) {
                            handleAmbulancesUpdate(ambulances)
                        } else {
                            _uiState.update { it.copy(ambulances = ambulances) }
                        }
                    }
                } catch (e: SecurityException) {
                    if (initialAmbulanceId != null) {
                        try {
                            val ambulance = repository.getAmbulanceById(initialAmbulanceId)
                            if (ambulance != null) {
                                repository.getAmbulancesByHospitalStream(ambulance.hospitalId).collect { ambulances ->
                                    if (!_uiState.value.requestSent) {
                                        handleAmbulancesUpdate(ambulances)
                                    } else {
                                        _uiState.update { it.copy(ambulances = ambulances) }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            _uiState.update { it.copy(error = e.message) }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
        uiState.value.userLocation?.let { findNearestEntities(it) }
    }

    private fun handleAmbulancesUpdate(ambulances: List<AmbulanceEntity>) {
        _uiState.update { it.copy(ambulances = ambulances) }
        if (initialAmbulanceId != null) {
            val selected = ambulances.find { it.id == initialAmbulanceId }
            _uiState.update { it.copy(selectedAmbulance = selected) }
            selected?.let { amb ->
                uiState.value.userLocation?.let { userLoc ->
                    fetchRoute(LatLng(amb.latitude, amb.longitude), userLoc)
                }
            }
        }
        uiState.value.userLocation?.let { findNearestEntities(it) }
    }

    fun updateUserLocation(location: LatLng, context: android.content.Context) {
        _uiState.update { it.copy(userLocation = location) }
        fetchAddress(location, context)
        if (_uiState.value.hospitals.isNotEmpty()) {
            findNearestEntities(location)
        }
    }

    private fun fetchAddress(location: LatLng, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressLine = address.getAddressLine(0) ?: "Unknown Location"
                            _uiState.update { it.copy(userAddress = addressLine) }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressLine = address.getAddressLine(0) ?: "Unknown Location"
                        _uiState.update { it.copy(userAddress = addressLine) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(userAddress = "Location identified") }
            }
        }
    }

    fun onConfirmRequest() {
        _uiState.update { it.copy(showConfirmationDialog = true) }
    }

    fun onConfirmDialog() {
        viewModelScope.launch {
            _uiState.update { it.copy(showConfirmationDialog = false, isLoading = true) }
            // Simulating API call/Request creation
            delay(1000)
            _uiState.update { it.copy(requestSent = true, isLoading = false) }
            startAmbulanceSimulation()
        }
    }



    fun onDismissDialog() {
        _uiState.update { it.copy(showConfirmationDialog = false) }
    }

    fun onCancelRequest() {
        viewModelScope.launch {
            simulationJob?.cancel()
            _uiState.update { it.copy(isLoading = true, requestSent = false) }
            // Simulating cancellation
            delay(1000)
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    fun onPairRequest() {
        viewModelScope.launch {
            simulationJob?.cancel()
            _uiState.update { it.copy(isLoading = true) }
            // Simulating completion/pairing
            delay(1000)
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    private fun findNearestEntities(userLatLng: LatLng) {
        val currentState = _uiState.value

        val nearestHospital = if (currentState.hospitals.isNotEmpty()) {
            currentState.hospitals.minByOrNull { hospital ->
                haversineKm(
                    userLatLng.latitude, userLatLng.longitude,
                    hospital.latitude ?: 0.0, hospital.longitude ?: 0.0
                )
            }
        } else null

        val nearestAmbulance = if (initialAmbulanceId != null) {
            currentState.selectedAmbulance
        } else if (currentState.ambulances.isNotEmpty()) {
            currentState.ambulances.minByOrNull { ambulance ->
                haversineKm(
                    userLatLng.latitude, userLatLng.longitude,
                    ambulance.latitude, ambulance.longitude
                )
            }
        } else null

        val distanceKm = if (nearestAmbulance != null) {
            haversineKm(
                userLatLng.latitude, userLatLng.longitude,
                nearestAmbulance.latitude, nearestAmbulance.longitude
            )
        } else null

        _uiState.update { it.copy(
            nearestHospital = nearestHospital,
            nearestAmbulance = nearestAmbulance,
            distanceKm = distanceKm
        ) }

        if (nearestAmbulance != null && !currentState.requestSent) {
            fetchRoute(
                origin = LatLng(nearestAmbulance.latitude, nearestAmbulance.longitude),
                destination = userLatLng
            )
        }
    }

    private fun interpolatePoints(points: List<LatLng>, stepDistanceKm: Double = 0.01): List<LatLng> {
        if (points.size < 2) return points
        val result = mutableListOf<LatLng>()
        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            
            val segmentDistance = haversineKm(
                start.latitude, start.longitude,
                end.latitude, end.longitude
            )
            
            // Generate coordinates every 'stepDistanceKm' (e.g., 10 meters)
            val numSteps = (segmentDistance / stepDistanceKm).toInt().coerceAtLeast(1)
            
            for (j in 0 until numSteps) {
                val fraction = j.toDouble() / numSteps
                val lat = start.latitude + (end.latitude - start.latitude) * fraction
                val lon = start.longitude + (end.longitude - start.longitude) * fraction
                result.add(LatLng(lat, lon))
            }
        }
        result.add(points.last())
        return result
    }

    private fun startAmbulanceSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val rawRoute = _uiState.value.routePoints
            val userLoc = _uiState.value.userLocation
            if (rawRoute.isEmpty() || userLoc == null) return@launch

            // Calculate "space coordinates" along the route at fixed 10m intervals
            val route = interpolatePoints(rawRoute, stepDistanceKm = 0.01)

            for (point in route) {
                if (!_uiState.value.requestSent) break
                
                val currentDistance = haversineKm(
                    point.latitude, point.longitude,
                    userLoc.latitude, userLoc.longitude
                )

                _uiState.update { state ->
                    val updatedAmbulance = state.nearestAmbulance?.copy(
                        latitude = point.latitude,
                        longitude = point.longitude
                    )
                    state.copy(
                        nearestAmbulance = updatedAmbulance,
                        distanceKm = currentDistance
                    )
                }
                delay(50) // Update every 50ms for high-frequency smooth movement
            }
        }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        viewModelScope.launch {
            try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destStr = "${destination.latitude},${destination.longitude}"
                val apiKey = BuildConfig.MAPS_API_KEY

                val response = withContext(Dispatchers.IO) {
                    directionsApi.getDirections(originStr, destStr, apiKey)
                }

                if (response.routes.isNotEmpty()) {
                    val encodedPolyline = response.routes[0].overview_polyline.points
                    val points = PolylineDecoder.decode(encodedPolyline)
                    _uiState.update { it.copy(routePoints = points) }
                } else {
                    _uiState.update { it.copy(routePoints = listOf(origin, destination)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(routePoints = listOf(origin, destination)) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
        hospitalsJob?.cancel()
        ambulancesJob?.cancel()
    }
}
