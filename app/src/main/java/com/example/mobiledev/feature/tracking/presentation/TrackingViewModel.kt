package com.example.mobiledev.feature.tracking.presentation

import android.location.Location
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
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
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
    val isLoading: Boolean = false,
    val error: String? = null
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

    init {
        observeTrackingData()
    }

    private fun observeTrackingData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Hospitals
            launch {
                try {
                    repository.getAllHospitalsStream().collect { hospitals ->
                        _uiState.update { it.copy(hospitals = hospitals) }
                        uiState.value.userLocation?.let { findNearestEntities(it) }
                    }
                } catch (e: SecurityException) {
                    repository.getApprovedHospitalsStream().collect { hospitals ->
                        _uiState.update { it.copy(hospitals = hospitals) }
                        uiState.value.userLocation?.let { findNearestEntities(it) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }

            // Ambulances
            launch {
                try {
                    repository.getAllAmbulancesStream().collect { ambulances ->
                        handleAmbulancesUpdate(ambulances)
                    }
                } catch (e: SecurityException) {
                    if (initialAmbulanceId != null) {
                        try {
                            val ambulance = repository.getAmbulanceById(initialAmbulanceId)
                            if (ambulance != null) {
                                repository.getAmbulancesByHospitalStream(ambulance.hospitalId).collect { ambulances ->
                                    handleAmbulancesUpdate(ambulances)
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

    fun updateUserLocation(location: LatLng) {
        _uiState.update { it.copy(userLocation = location) }
        if (_uiState.value.hospitals.isNotEmpty()) {
            findNearestEntities(location)
        }
    }

    private fun findNearestEntities(userLatLng: LatLng) {
        val currentState = _uiState.value
        if (currentState.hospitals.isEmpty()) return

        val nearestHospital = currentState.hospitals.minByOrNull { hospital ->
            val results = FloatArray(1)
            Location.distanceBetween(
                userLatLng.latitude, userLatLng.longitude,
                hospital.latitude ?: 0.0, hospital.longitude ?: 0.0,
                results
            )
            results[0]
        }

        val nearestAmbulance = if (initialAmbulanceId != null) {
            currentState.selectedAmbulance
        } else if (currentState.ambulances.isNotEmpty()) {
            currentState.ambulances.minByOrNull { ambulance ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    userLatLng.latitude, userLatLng.longitude,
                    ambulance.latitude, ambulance.longitude,
                    results
                )
                results[0]
            }
        } else null

        _uiState.update { it.copy(
            nearestHospital = nearestHospital,
            nearestAmbulance = nearestAmbulance
        ) }

        if (nearestAmbulance != null) {
            fetchRoute(
                origin = LatLng(nearestAmbulance.latitude, nearestAmbulance.longitude),
                destination = userLatLng
            )
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
}
