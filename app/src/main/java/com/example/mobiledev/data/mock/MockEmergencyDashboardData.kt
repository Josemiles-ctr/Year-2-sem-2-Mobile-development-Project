package com.example.mobiledev.data.mock

import com.example.mobiledev.data.model.Ambulance
import com.example.mobiledev.data.model.AmbulanceStatus
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus

object MockEmergencyDashboardData {

    private val now: Long
        get() = System.currentTimeMillis()

    val ambulances: List<Ambulance>
        get() = listOf(
            Ambulance(
                id = "AMB-MOCK-01",
                plateNumber = "KAA 411A",
                driverName = "Derek O.",
                status = AmbulanceStatus.AVAILABLE
            ),
            Ambulance(
                id = "AMB-MOCK-02",
                plateNumber = "KBB 219P",
                driverName = "Emma N.",
                status = AmbulanceStatus.ON_MISSION,
                currentEmergencyId = "REQ-MOCK-1002"
            ),
            Ambulance(
                id = "AMB-MOCK-03",
                plateNumber = "KCC 774T",
                driverName = "Brian K.",
                status = AmbulanceStatus.AVAILABLE
            ),
            Ambulance(
                id = "AMB-MOCK-04",
                plateNumber = "KDD 560M",
                driverName = "Cynthia A.",
                status = AmbulanceStatus.MAINTENANCE
            ),
            Ambulance(
                id = "AMB-MOCK-05",
                plateNumber = "KEE 902L",
                driverName = "Moses P.",
                status = AmbulanceStatus.ON_MISSION,
                currentEmergencyId = "REQ-MOCK-1005"
            )
        )

    private fun baseRequests(): List<EmergencyRequest> {
        val timestamp = now
        return listOf(
            EmergencyRequest(
                id = "REQ-MOCK-1001",
                patientName = "Grace N.",
                location = "Kampala Road, CBD",
                phoneNumber = "0701122334",
                description = "Severe chest pain and dizziness.",
                status = EmergencyStatus.PENDING,
                timestamp = timestamp - 6 * 60 * 1000L
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1002",
                patientName = "Joseph M.",
                location = "Kololo Hill Drive",
                phoneNumber = "0707788445",
                description = "Road traffic accident with bleeding.",
                status = EmergencyStatus.ASSIGNED,
                timestamp = timestamp - 12 * 60 * 1000L,
                assignedAmbulanceId = "AMB-MOCK-02"
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1003",
                patientName = "Aisha K.",
                location = "Ntinda Trading Center",
                phoneNumber = "0709211882",
                description = "Breathing difficulty and wheezing.",
                status = EmergencyStatus.EN_ROUTE,
                timestamp = timestamp - 18 * 60 * 1000L,
                assignedAmbulanceId = "AMB-MOCK-03"
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1004",
                patientName = "Peter T.",
                location = "Wandegeya Market",
                phoneNumber = "0703344556",
                description = "Unconscious adult found on roadside.",
                status = EmergencyStatus.ARRIVED,
                timestamp = timestamp - 27 * 60 * 1000L,
                assignedAmbulanceId = "AMB-MOCK-01"
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1005",
                patientName = "Mary A.",
                location = "Najjera Estate",
                phoneNumber = "0709988776",
                description = "Suspected stroke with slurred speech.",
                status = EmergencyStatus.COMPLETED,
                timestamp = timestamp - 49 * 60 * 1000L,
                assignedAmbulanceId = "AMB-MOCK-05"
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1006",
                patientName = "Daniel R.",
                location = "Muyenga Tank Hill",
                phoneNumber = "0700012211",
                description = "Minor injury, request cancelled by caller.",
                status = EmergencyStatus.CANCELLED,
                timestamp = timestamp - 70 * 60 * 1000L
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1007",
                patientName = "Sarah B.",
                location = "Nakasero Hospital Road",
                phoneNumber = "0703321456",
                description = "High fever and convulsions in child.",
                status = EmergencyStatus.PENDING,
                timestamp = timestamp - 3 * 60 * 1000L
            ),
            EmergencyRequest(
                id = "REQ-MOCK-1008",
                patientName = "Ronald C.",
                location = "Makerere Main Gate",
                phoneNumber = "0708456123",
                description = "Possible fracture after fall from stairs.",
                status = EmergencyStatus.ASSIGNED,
                timestamp = timestamp - 35 * 60 * 1000L,
                assignedAmbulanceId = "AMB-MOCK-01"
            )
        )
    }

    fun getEmergencyRequests(
        status: String?,
        dateFrom: Long?,
        dateTo: Long?,
        limit: Int,
        offset: Int
    ): List<EmergencyRequest> {
        var filtered = baseRequests().sortedByDescending { it.timestamp }

        if (!status.isNullOrBlank()) {
            filtered = filtered.filter { it.status.name == status }
        }

        if (dateFrom != null) {
            filtered = filtered.filter { it.timestamp >= dateFrom }
        }

        if (dateTo != null) {
            filtered = filtered.filter { it.timestamp <= dateTo }
        }

        return filtered.drop(offset).take(limit)
    }
}
