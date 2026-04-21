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
        val names = listOf(
            "Grace N.", "Joseph M.", "Aisha K.", "Peter T.", "Mary A.",
            "Daniel R.", "Sarah B.", "Ronald C.", "Faith L.", "Kevin O.",
            "Martha I.", "Brian W."
        )
        val locations = listOf(
            "Kampala Road, CBD",
            "Kololo Hill Drive",
            "Ntinda Trading Center",
            "Wandegeya Market",
            "Najjera Estate",
            "Muyenga Tank Hill",
            "Nakasero Hospital Road",
            "Makerere Main Gate",
            "Bukoto Junction",
            "Kisaasi Central"
        )
        val descriptions = listOf(
            "Severe chest pain and dizziness.",
            "Road traffic accident with bleeding.",
            "Breathing difficulty and wheezing.",
            "Unconscious adult found on roadside.",
            "Suspected stroke with slurred speech.",
            "High fever and convulsions in child.",
            "Possible fracture after fall from stairs.",
            "Persistent abdominal pain and vomiting."
        )
        val statuses = listOf(
            EmergencyStatus.PENDING,
            EmergencyStatus.ASSIGNED,
            EmergencyStatus.EN_ROUTE,
            EmergencyStatus.ARRIVED,
            EmergencyStatus.COMPLETED,
            EmergencyStatus.CANCELLED
        )

        return buildList {
            var index = 0
            statuses.forEach { status ->
                repeat(10) {
                    val requestNumber = 1001 + index
                    val assignedAmbulanceId = when (status) {
                        EmergencyStatus.ASSIGNED -> "AMB-MOCK-01"
                        EmergencyStatus.EN_ROUTE -> "AMB-MOCK-03"
                        EmergencyStatus.ARRIVED -> "AMB-MOCK-02"
                        EmergencyStatus.COMPLETED -> "AMB-MOCK-05"
                        else -> null
                    }

                    add(
                        EmergencyRequest(
                            id = "REQ-MOCK-$requestNumber",
                            patientName = names[index % names.size],
                            location = locations[index % locations.size],
                            phoneNumber = "07${(10000000 + index).toString().takeLast(8)}",
                            description = descriptions[index % descriptions.size],
                            status = status,
                            timestamp = timestamp - (index + 1) * 4L * 60 * 1000L,
                            assignedAmbulanceId = assignedAmbulanceId
                        )
                    )

                    index += 1
                }
            }
        }
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
