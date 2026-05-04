package com.example.mobiledev.data.mock

import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import com.example.mobiledev.data.local.entity.UserEntity

data class MockSeedBundle(
    val hospitals: List<HospitalEntity>,
    val users: List<UserEntity>,
    val ambulances: List<AmbulanceEntity>,
    val requests: List<EmergencyRequestEntity>
)

object MockSeedData {

    fun create(now: Long, passwordHash: String): MockSeedBundle {
        val hospitals = listOf(
            HospitalEntity("HOSPITAL_1", "ADMIN_1", "Mulago National Referral", "mulago@health.go.ug", "041-4541511", "Mulago Hill Road, Kampala", 0.3381, 32.5761, "UUID_HOSP_1", passwordHash, HospitalStatus.APPROVED, 3, now - 900_000, now - 300_000),
            HospitalEntity("HOSPITAL_2", "ADMIN_2", "Case Medical Centre", "info@casemedical.co.ug", "031-2250700", "Buganda Road, Kampala", 0.3162, 32.5762, "UUID_HOSP_2", passwordHash, HospitalStatus.APPROVED, 2, now - 890_000, now - 290_000),
            HospitalEntity("HOSPITAL_3", "ADMIN_3", "IHK Kampala", "info@img.co.ug", "031-2200400", "Plot 4684, Barnabas Road, Namuwongo", 0.3015, 32.6080, "UUID_HOSP_3", passwordHash, HospitalStatus.APPROVED, 2, now - 880_000, now - 280_000),
            HospitalEntity("HOSPITAL_4", "ADMIN_4", "Mengo Hospital", "mengo@health.org", "041-4270222", "Namirembe Hill, Kampala", 0.3125, 32.5585, "UUID_HOSP_4", passwordHash, HospitalStatus.APPROVED, 3, now - 870_000, now - 270_000),
            HospitalEntity("HOSPITAL_5", "ADMIN_5", "Rubaga Hospital", "info@rubagahospital.org", "041-4270203", "Rubaga Hill, Kampala", 0.3031, 32.5531, "UUID_HOSP_5", passwordHash, HospitalStatus.PENDING, 0, now - 860_000, now - 260_000),
            HospitalEntity("HOSPITAL_6", "ADMIN_6", "Nsambya Hospital", "info@nsambyahospital.or.ug", "041-4267012", "Nsambya Estate Road, Kampala", 0.2995, 32.5878, "UUID_HOSP_6", passwordHash, HospitalStatus.APPROVED, 1, now - 850_000, now - 250_000),
            HospitalEntity("HOSPITAL_7", "ADMIN_7", "Kawempe General Hospital", "kawempe@health.go.ug", "041-4567890", "Kawempe, Kampala", 0.3750, 32.5600, "UUID_HOSP_7", passwordHash, HospitalStatus.REJECTED, 0, now - 840_000, now - 240_000),
            HospitalEntity("HOSPITAL_8", "ADMIN_8", "Naguru Referral Hospital", "naguru@health.go.ug", "041-4123456", "Naguru, Kampala", 0.3450, 32.6100, "UUID_HOSP_8", passwordHash, HospitalStatus.APPROVED, 1, now - 830_000, now - 230_000),
            HospitalEntity("HOSPITAL_9", "ADMIN_9", "Victoria Hospital", "info@victoria-hospital.com", "031-2200900", "Plot 18, Bukoto-Kisaasi Road", 0.3520, 32.6020, "UUID_HOSP_9", passwordHash, HospitalStatus.APPROVED, 1, now - 820_000, now - 220_000),
            HospitalEntity("HOSPITAL_10", "ADMIN_10", "Lubaga Medical Center", "info@lubaga.org", "041-4321098", "Lubaga, Kampala", 0.3050, 32.5500, "UUID_HOSP_10", passwordHash, HospitalStatus.APPROVED, 1, now - 810_000, now - 210_000)
        )

        val users = listOf(
            UserEntity("ADMIN_1", "HOSPITAL_1", "Kato Joseph (Admin)", "kato@mulago.ug", "0772123456", "Mulago", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_1", now - 800_000, now - 200_000),
            UserEntity("ADMIN_2", "HOSPITAL_2", "Nakamya Sarah (Admin)", "sarah@casemedical.ug", "0782654321", "Central Kampala", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_2", now - 799_000, now - 199_000),
            UserEntity("DRIVER_1", "HOSPITAL_1", "Opio Brian & Lwanga Moses", "crew1@mulago.ug", "0701111222", "Mulago Station", "DRIVER", "UUID_USER_DRIVER_1", now - 798_000, now - 198_000),
            UserEntity("DRIVER_2", "HOSPITAL_1", "Nabirye Esther & Atwine Chris", "crew2@mulago.ug", "0703333444", "Mulago Station", "DRIVER", "UUID_USER_DRIVER_2", now - 797_000, now - 197_000),
            UserEntity("DRIVER_3", "HOSPITAL_2", "Mwesigwa Robert & Ssemuwemba Isaac", "crew3@casemedical.ug", "0705555666", "Central Station", "DRIVER", "UUID_USER_DRIVER_3", now - 796_000, now - 196_000),
            UserEntity("DRIVER_4", "HOSPITAL_2", "Okello David & Anyango Grace", "crew4@casemedical.ug", "0707777888", "Central Station", "DRIVER", "UUID_USER_DRIVER_4", now - 795_000, now - 195_000),
            UserEntity("PATIENT_1", null, "Mugisha Peter", "peter@patient.ug", "0774000111", "Wandegeya", "PATIENT", "UUID_USER_PATIENT_1", now - 794_000, now - 194_000),
            UserEntity("PATIENT_2", null, "Namukasa Faith", "faith@patient.ug", "0774000222", "Kololo", "PATIENT", "UUID_USER_PATIENT_2", now - 793_000, now - 193_000),
            UserEntity("PATIENT_3", null, "Kimbugwe Ronald", "ronald@patient.ug", "0774000333", "Ntinda", "PATIENT", "UUID_USER_PATIENT_3", now - 792_000, now - 192_000),
            UserEntity("PATIENT_4", null, "Akello Martha", "martha@patient.ug", "0774000444", "Nakasero", "PATIENT", "UUID_USER_PATIENT_4", now - 791_000, now - 191_000)
        )

        val ambulances = listOf(
            AmbulanceEntity("AMB_1", "HOSPITAL_1", "DRIVER_1", "UBA 101A", "LIC-UGA-101", "AVAILABLE", 0.3390, 32.5770, now - 700_000, now - 180_000),
            AmbulanceEntity("AMB_2", "HOSPITAL_1", "DRIVER_2", "UBB 202B", "LIC-UGA-202", "ON_EMERGENCY", 0.3400, 32.5780, now - 699_000, now - 179_000),
            AmbulanceEntity("AMB_3", "HOSPITAL_2", "DRIVER_3", "UBC 303C", "LIC-UGA-303", "AVAILABLE", 0.3170, 32.5770, now - 698_000, now - 178_000),
            AmbulanceEntity("AMB_4", "HOSPITAL_2", "DRIVER_4", "UBD 404D", "LIC-UGA-404", "OFFLINE", 0.3180, 32.5780, now - 697_000, now - 177_000),
            AmbulanceEntity("AMB_5", "HOSPITAL_3", "DRIVER_1", "UBE 505E", "LIC-UGA-505", "AVAILABLE", 0.3020, 32.6090, now - 696_000, now - 176_000),
            AmbulanceEntity("AMB_6", "HOSPITAL_3", "DRIVER_2", "UBF 606F", "LIC-UGA-606", "AVAILABLE", 0.3030, 32.6100, now - 695_000, now - 175_000),
            AmbulanceEntity("AMB_7", "HOSPITAL_4", "DRIVER_3", "UBG 707G", "LIC-UGA-707", "ON_EMERGENCY", 0.3130, 32.5590, now - 694_000, now - 174_000),
            AmbulanceEntity("AMB_8", "HOSPITAL_4", "DRIVER_4", "UBH 808H", "LIC-UGA-808", "AVAILABLE", 0.3140, 32.5600, now - 693_000, now - 173_000),
            AmbulanceEntity("AMB_9", "HOSPITAL_1", "DRIVER_1", "UBI 909I", "LIC-UGA-909", "OFFLINE", 0.3370, 32.5750, now - 692_000, now - 172_000),
            AmbulanceEntity("AMB_10", "HOSPITAL_2", "DRIVER_2", "UBJ 100J", "LIC-UGA-100", "AVAILABLE", 0.3150, 32.5750, now - 691_000, now - 171_000)
        )

        val requests = listOf(
            // Hospital 1 - Mulago
            EmergencyRequestEntity("REQ_1", "PATIENT_1", "HOSPITAL_1", null, "PENDING", "Severe chest pain near Posta", "Kampala Road", 0.3136, 32.5811, "CRITICAL", null, now - 600_000, now - 160_000, null, false),
            EmergencyRequestEntity("REQ_2", "PATIENT_2", "HOSPITAL_1", "AMB_2", "ASSIGNED", "Boda boda accident", "Jinja Road", 0.3190, 32.5950, "HIGH", 9, now - 599_000, now - 159_000, null, false),
            EmergencyRequestEntity("REQ_11", "PATIENT_3", "HOSPITAL_1", null, "PENDING", "Acute difficulty breathing", "Wandegeya", 0.3300, 32.5730, "CRITICAL", null, now - 150_000, now - 100_000, null, false),
            EmergencyRequestEntity("REQ_12", "PATIENT_4", "HOSPITAL_1", null, "PENDING", "Persistent high fever", "Makerere", 0.3330, 32.5670, "MEDIUM", null, now - 250_000, now - 110_000, null, false),
            EmergencyRequestEntity("REQ_13", "PATIENT_1", "HOSPITAL_1", "AMB_1", "EN_ROUTE", "Minor limb injury", "City Center", 0.3130, 32.5800, "LOW", 15, now - 350_000, now - 120_000, null, false),

            // Hospital 2 - Case Medical
            EmergencyRequestEntity("REQ_3", "PATIENT_3", "HOSPITAL_2", null, "PENDING", "Respiratory difficulty", "Ntinda Market", 0.3540, 32.6110, "HIGH", null, now - 598_000, now - 158_000, null, false),
            EmergencyRequestEntity("REQ_4", "PATIENT_4", "HOSPITAL_2", "AMB_3", "ARRIVED", "Fall from height", "Makerere Main Gate", 0.3330, 32.5670, "MEDIUM", 6, now - 597_000, now - 157_000, null, false),
            EmergencyRequestEntity("REQ_14", "PATIENT_2", "HOSPITAL_2", null, "PENDING", "Severe allergic reaction", "Nakasero", 0.3240, 32.5800, "CRITICAL", null, now - 450_000, now - 130_000, null, false),
            EmergencyRequestEntity("REQ_15", "PATIENT_3", "HOSPITAL_2", null, "PENDING", "Fractured arm", "Kololo", 0.3380, 32.5950, "MEDIUM", null, now - 550_000, now - 140_000, null, false),
            EmergencyRequestEntity("REQ_10", "PATIENT_2", "HOSPITAL_2", null, "PENDING", "Unconscious patient", "Kololo", 0.3380, 32.5950, "CRITICAL", null, now - 591_000, now - 151_000, null, false),

            // Hospital 3 - IHK
            EmergencyRequestEntity("REQ_5", "PATIENT_1", "HOSPITAL_3", "AMB_5", "ASSIGNED", "Suspected malaria crisis", "Namuwongo", 0.2980, 32.6100, "CRITICAL", 7, now - 596_000, now - 156_000, null, false),
            EmergencyRequestEntity("REQ_6", "PATIENT_2", "HOSPITAL_3", null, "PENDING", "Acute asthma attack", "Bugolobi", 0.3200, 32.6250, "MEDIUM", null, now - 595_000, now - 155_000, null, false),
            EmergencyRequestEntity("REQ_16", "PATIENT_3", "HOSPITAL_3", null, "PENDING", "Deep laceration", "Namuwongo", 0.3015, 32.6080, "HIGH", null, now - 650_000, now - 160_000, null, false),

            // Hospital 4 - Mengo
            EmergencyRequestEntity("REQ_7", "PATIENT_3", "HOSPITAL_4", "AMB_7", "ON_WAY", "Heavy bleeding", "Wandegeya", 0.3300, 32.5730, "HIGH", 5, now - 594_000, now - 154_000, null, false),
            EmergencyRequestEntity("REQ_8", "PATIENT_4", "HOSPITAL_4", "AMB_8", "COMPLETED", "Minor injury", "Nakasero", 0.3240, 32.5800, "LOW", 12, now - 593_000, now - 153_000, now - 120_000, false),
            EmergencyRequestEntity("REQ_17", "PATIENT_1", "HOSPITAL_4", null, "PENDING", "Sudden collapse", "Rubaga", 0.3031, 32.5531, "CRITICAL", null, now - 750_000, now - 170_000, null, false),
            EmergencyRequestEntity("REQ_18", "PATIENT_2", "HOSPITAL_4", null, "PENDING", "Abdominal pain", "Mengo", 0.3125, 32.5585, "LOW", null, now - 850_000, now - 180_000, null, false)
        )

        return MockSeedBundle(
            hospitals = hospitals,
            users = users,
            ambulances = ambulances,
            requests = requests
        )
    }
}
