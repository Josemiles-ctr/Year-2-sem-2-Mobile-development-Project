package com.example.mobiledev.feature.patient.presentation

import com.example.mobiledev.data.location.Coordinates
import com.example.mobiledev.data.local.entity.HospitalEntity

fun filterHospitalsByQuery(
    hospitals: List<HospitalEntity>,
    query: String
): List<HospitalEntity> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return hospitals

    return hospitals.filter { hospital ->
        hospital.name.contains(normalizedQuery, ignoreCase = true) ||
            hospital.location.contains(normalizedQuery, ignoreCase = true) ||
            hospital.phone.contains(normalizedQuery, ignoreCase = true)
    }
}

fun sortHospitalsByDistance(
    hospitals: List<HospitalEntity>,
    currentLocation: Coordinates?
): List<HospitalEntity> {
    if (currentLocation == null) return hospitals

    return hospitals.sortedWith(
        compareBy<HospitalEntity> { hospital ->
            currentLocation.let { hospital.distanceTo(it) } ?: Double.MAX_VALUE
        }.thenBy { it.name.lowercase() }
    )
}

fun hospitalDistanceKm(
    hospital: HospitalEntity,
    currentLocation: Coordinates?
): Double? = currentLocation?.let { hospital.distanceTo(it) }

private fun HospitalEntity.distanceTo(location: Coordinates): Double? {
    val hospitalLatitude = latitude ?: return null
    val hospitalLongitude = longitude ?: return null
    return haversineKm(
        location.latitude,
        location.longitude,
        hospitalLatitude,
        hospitalLongitude
    )
}

private fun haversineKm(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): Double {
    val earthRadiusKm = 6371.0
    val latDiff = Math.toRadians(endLat - startLat)
    val lonDiff = Math.toRadians(endLon - startLon)
    val a = Math.sin(latDiff / 2).let { sinLat -> sinLat * sinLat } +
        Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
        Math.sin(lonDiff / 2).let { sinLon -> sinLon * sinLon }
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadiusKm * c
}





