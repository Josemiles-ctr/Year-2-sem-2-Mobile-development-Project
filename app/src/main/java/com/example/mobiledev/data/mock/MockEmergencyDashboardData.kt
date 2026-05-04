package com.example.mobiledev.data.mock

import com.example.mobiledev.data.model.Ambulance
import com.example.mobiledev.data.model.AmbulanceStatus
import com.example.mobiledev.data.model.EmergencyPriority
import com.example.mobiledev.data.model.EmergencyRequest
import com.example.mobiledev.data.model.EmergencyStatus

object MockEmergencyDashboardData {

    private val now: Long
        get() = System.currentTimeMillis()

    val ambulances: List<Ambulance>
        get() = listOf(
            Ambulance(
                id = "AMB-UGA-01",
                plateNumber = "UBA 411A",
                drivers = "Kato Joseph, Lwanga Moses",
                status = AmbulanceStatus.AVAILABLE
            ),
            Ambulance(
                id = "AMB-UGA-02",
                plateNumber = "UBB 219P",
                drivers = "Nabirye Esther, Opio Brian",
                status = AmbulanceStatus.ON_MISSION,
                currentEmergencyId = "REQ-UGA-1002"
            ),
            Ambulance(
                id = "AMB-UGA-03",
                plateNumber = "UBC 774T",
                drivers = "Mwesigwa Robert, Atwine Chris",
                status = AmbulanceStatus.AVAILABLE
            ),
            Ambulance(
                id = "AMB-UGA-04",
                plateNumber = "UBD 560M",
                drivers = "Ssemuwemba Isaac, Okello David",
                status = AmbulanceStatus.MAINTENANCE
            ),
            Ambulance(
                id = "AMB-UGA-05",
                plateNumber = "UBE 902L",
                drivers = "Nakamya Jane, Musoke Peter",
                status = AmbulanceStatus.ON_MISSION,
                currentEmergencyId = "REQ-UGA-1005"
            )
        )

    private fun baseRequests(): List<EmergencyRequest> {
        val timestamp = now
        val names = listOf(
            "Mugisha Peter", "Namukasa Faith", "Kimbugwe Ronald", "Akello Martha", "Otim Denis",
            "Nantongo Sarah", "Bwambale John", "Atim Grace", "Mutebi Charles", "Nalubega Prossy",
            "Okot Patrick", "Alowo Stella"
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
            "Bugolobi Village",
            "Kisaasi Central"
        )
        val descriptions = listOf(
            "Severe chest pain near Posta Uganda.",
            "Boda boda accident along Jinja Road.",
            "Respiratory distress in Kololo.",
            "Unconscious adult found at taxi park.",
            "Suspected stroke at Garden City.",
            "Child with high fever in Kamwokya.",
            "Fall from height at construction site.",
            "Acute abdominal pain in Ntinda."
        )
        val priorities = listOf(
            EmergencyPriority.CRITICAL,
            EmergencyPriority.HIGH,
            EmergencyPriority.MEDIUM,
            EmergencyPriority.LOW
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
                        EmergencyStatus.ASSIGNED -> "AMB-UGA-01"
                        EmergencyStatus.EN_ROUTE -> "AMB-UGA-03"
                        EmergencyStatus.ARRIVED -> "AMB-UGA-02"
                        EmergencyStatus.COMPLETED -> "AMB-UGA-05"
                        else -> null
                    }

                    add(
                        EmergencyRequest(
                            id = "REQ-UGA-$requestNumber",
                            patientName = names[index % names.size],
                            location = locations[index % locations.size],
                            phoneNumber = "0772${(100000 + index).toString().takeLast(6)}",
                            description = descriptions[index % descriptions.size],
                            status = status,
                            priority = priorities[index % priorities.size],
                            timestamp = timestamp - (index + 1) * 4L * 60 * 1000L,
                            assignedAmbulanceId = assignedAmbulanceId,
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
