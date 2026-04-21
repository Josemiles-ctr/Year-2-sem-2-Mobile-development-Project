package com.example.mobiledev.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledev.data.local.dao.AmbulanceDao
import com.example.mobiledev.data.local.dao.EmergencyRequestDao
import com.example.mobiledev.data.local.dao.HospitalDao
import com.example.mobiledev.data.local.dao.UserDao
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.UserEntity
import com.example.mobiledev.data.local.entity.HospitalStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

@Database(
    entities = [
        UserEntity::class,
        HospitalEntity::class,
        AmbulanceEntity::class,
        EmergencyRequestEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(ResQDatabase.Converters::class)
abstract class ResQDatabase : RoomDatabase() {

    class Converters {
        @TypeConverter
        fun fromHospitalStatus(status: HospitalStatus): String = status.name

        @TypeConverter
        fun toHospitalStatus(value: String): HospitalStatus = HospitalStatus.valueOf(value)
    }

    abstract fun userDao(): UserDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun ambulanceDao(): AmbulanceDao
    abstract fun emergencyRequestDao(): EmergencyRequestDao

    companion object {
        @Volatile
        private var INSTANCE: ResQDatabase? = null

        fun getDatabase(context: Context): ResQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResQDatabase::class.java,
                    "resq_database"
                )
                .addCallback(DatabaseCallback(context))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        
        override fun onCreate(db: SupportSQLiteDatabase) {
            // Use a post-initialization trigger to seed via DAOs once the DB is ready
            CoroutineScope(Dispatchers.IO).launch {
                getDatabase(context).let { database ->
                    try {
                        seedData(database)
                        Log.d("ResQDatabase", "Initial seeding successful")
                    } catch (e: Exception) {
                        Log.e("ResQDatabase", "Initial seeding failed: ${e.message}")
                    }
                }
            }
        }

        suspend fun seedData(db: ResQDatabase) {
            val userDao = db.userDao()
            val hospitalDao = db.hospitalDao()
            val ambulanceDao = db.ambulanceDao()
            val requestDao = db.emergencyRequestDao()

            val hashedPW = BCrypt.hashpw("password123", BCrypt.gensalt())
            val now = System.currentTimeMillis()

            val hospitals = listOf(
                HospitalEntity("HOSPITAL_1", "ADMIN_1", "City Central Hospital", "hospital1@resq.local", "555-0100", "123 Healthcare Ave", 40.7128, -74.0060, "UUID_HOSP_1", hashedPW, HospitalStatus.APPROVED, 3, now - 900_000, now - 300_000),
                HospitalEntity("HOSPITAL_2", "ADMIN_2", "Northside Medical", "hospital2@resq.local", "555-0101", "78 North Street", 40.7306, -73.9352, "UUID_HOSP_2", hashedPW, HospitalStatus.APPROVED, 2, now - 890_000, now - 290_000),
                HospitalEntity("HOSPITAL_3", "ADMIN_3", "West End General", "hospital3@resq.local", "555-0102", "91 West End Blvd", 40.7350, -74.0020, "UUID_HOSP_3", hashedPW, HospitalStatus.APPROVED, 2, now - 880_000, now - 280_000),
                HospitalEntity("HOSPITAL_4", "ADMIN_4", "East River Clinic", "hospital4@resq.local", "555-0103", "14 River Road", 40.7210, -73.9800, "UUID_HOSP_4", hashedPW, HospitalStatus.APPROVED, 3, now - 870_000, now - 270_000),
                HospitalEntity("HOSPITAL_5", "ADMIN_5", "South Metro Hospital", "hospital5@resq.local", "555-0104", "501 South Metro", 40.7001, -73.9901, "UUID_HOSP_5", hashedPW, HospitalStatus.PENDING, 0, now - 860_000, now - 260_000),
                HospitalEntity("HOSPITAL_6", "ADMIN_6", "Lakeside Emergency", "hospital6@resq.local", "555-0105", "8 Lake View", 40.7450, -73.9700, "UUID_HOSP_6", hashedPW, HospitalStatus.APPROVED, 1, now - 850_000, now - 250_000),
                HospitalEntity("HOSPITAL_7", "ADMIN_7", "Greenfield Health", "hospital7@resq.local", "555-0106", "300 Greenfield", 40.7421, -73.9550, "UUID_HOSP_7", hashedPW, HospitalStatus.REJECTED, 0, now - 840_000, now - 240_000),
                HospitalEntity("HOSPITAL_8", "ADMIN_8", "Harborpoint Medical", "hospital8@resq.local", "555-0107", "22 Harbor St", 40.7105, -74.0150, "UUID_HOSP_8", hashedPW, HospitalStatus.APPROVED, 1, now - 830_000, now - 230_000),
                HospitalEntity("HOSPITAL_9", "ADMIN_9", "Hillview Trauma", "hospital9@resq.local", "555-0108", "44 Hillview Lane", 40.7601, -73.9802, "UUID_HOSP_9", hashedPW, HospitalStatus.APPROVED, 1, now - 820_000, now - 220_000),
                HospitalEntity("HOSPITAL_10", "ADMIN_10", "Sunrise Care Center", "hospital10@resq.local", "555-0109", "77 Sunrise Ave", 40.7511, -73.9402, "UUID_HOSP_10", hashedPW, HospitalStatus.APPROVED, 1, now - 810_000, now - 210_000)
            )

            hospitals.forEach { hospital ->
                if (hospitalDao.getHospitalById(hospital.id) == null) {
                    hospitalDao.insertHospital(hospital)
                }
            }

            val users = listOf(
                UserEntity("ADMIN_1", "HOSPITAL_1", "Alice Admin", "admin1@resq.local", "555-1001", "City Central Hospital", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_1", now - 800_000, now - 200_000),
                UserEntity("ADMIN_2", "HOSPITAL_2", "Brian Admin", "admin2@resq.local", "555-1002", "Northside Medical", "HOSPITAL_ADMIN", "UUID_USER_ADMIN_2", now - 799_000, now - 199_000),
                UserEntity("DRIVER_1", "HOSPITAL_1", "Derek Driver", "driver1@resq.local", "555-1101", "Station A", "DRIVER", "UUID_USER_DRIVER_1", now - 798_000, now - 198_000),
                UserEntity("DRIVER_2", "HOSPITAL_1", "Diana Driver", "driver2@resq.local", "555-1102", "Station A", "DRIVER", "UUID_USER_DRIVER_2", now - 797_000, now - 197_000),
                UserEntity("DRIVER_3", "HOSPITAL_2", "Evan Driver", "driver3@resq.local", "555-1103", "Station B", "DRIVER", "UUID_USER_DRIVER_3", now - 796_000, now - 196_000),
                UserEntity("DRIVER_4", "HOSPITAL_2", "Emma Driver", "driver4@resq.local", "555-1104", "Station B", "DRIVER", "UUID_USER_DRIVER_4", now - 795_000, now - 195_000),
                UserEntity("PATIENT_1", null, "Paul Patient", "patient1@resq.local", "555-1201", "Times Square", "PATIENT", "UUID_USER_PATIENT_1", now - 794_000, now - 194_000),
                UserEntity("PATIENT_2", null, "Priya Patient", "patient2@resq.local", "555-1202", "Central Park", "PATIENT", "UUID_USER_PATIENT_2", now - 793_000, now - 193_000),
                UserEntity("PATIENT_3", null, "Peter Patient", "patient3@resq.local", "555-1203", "Wall Street", "PATIENT", "UUID_USER_PATIENT_3", now - 792_000, now - 192_000),
                UserEntity("PATIENT_4", null, "Pam Patient", "patient4@resq.local", "555-1204", "Queens Blvd", "PATIENT", "UUID_USER_PATIENT_4", now - 791_000, now - 191_000)
            )

            users.forEach { user ->
                if (userDao.getUserById(user.id) == null) {
                    userDao.insertUser(user)
                }
            }

            val ambulances = listOf(
                AmbulanceEntity("AMB_1", "HOSPITAL_1", "DRIVER_1", "REG-1001", "LIC-1001", "AVAILABLE", 40.7130, -74.0065, now - 700_000, now - 180_000),
                AmbulanceEntity("AMB_2", "HOSPITAL_1", "DRIVER_2", "REG-1002", "LIC-1002", "ON_EMERGENCY", 40.7140, -74.0055, now - 699_000, now - 179_000),
                AmbulanceEntity("AMB_3", "HOSPITAL_2", "DRIVER_3", "REG-2001", "LIC-2001", "AVAILABLE", 40.7301, -73.9360, now - 698_000, now - 178_000),
                AmbulanceEntity("AMB_4", "HOSPITAL_2", "DRIVER_4", "REG-2002", "LIC-2002", "OFFLINE", 40.7310, -73.9340, now - 697_000, now - 177_000),
                AmbulanceEntity("AMB_5", "HOSPITAL_3", "DRIVER_1", "REG-3001", "LIC-3001", "AVAILABLE", 40.7355, -74.0018, now - 696_000, now - 176_000),
                AmbulanceEntity("AMB_6", "HOSPITAL_3", "DRIVER_2", "REG-3002", "LIC-3002", "AVAILABLE", 40.7361, -74.0030, now - 695_000, now - 175_000),
                AmbulanceEntity("AMB_7", "HOSPITAL_4", "DRIVER_3", "REG-4001", "LIC-4001", "ON_EMERGENCY", 40.7215, -73.9810, now - 694_000, now - 174_000),
                AmbulanceEntity("AMB_8", "HOSPITAL_4", "DRIVER_4", "REG-4002", "LIC-4002", "AVAILABLE", 40.7220, -73.9795, now - 693_000, now - 173_000),
                AmbulanceEntity("AMB_9", "HOSPITAL_1", "DRIVER_1", "REG-1003", "LIC-1003", "OFFLINE", 40.7122, -74.0071, now - 692_000, now - 172_000),
                AmbulanceEntity("AMB_10", "HOSPITAL_2", "DRIVER_2", "REG-2003", "LIC-2003", "AVAILABLE", 40.7298, -73.9348, now - 691_000, now - 171_000)
            )

            ambulances.forEach { ambulance ->
                if (ambulanceDao.getAmbulanceById(ambulance.id) == null) {
                    ambulanceDao.insertAmbulance(ambulance)
                }
            }

            val requests = listOf(
                EmergencyRequestEntity("REQ_1", "PATIENT_1", "HOSPITAL_1", null, "PENDING", "Severe chest pain", "Times Square", 40.7580, -73.9855, "CRITICAL", null, now - 600_000, now - 160_000, null, false),
                EmergencyRequestEntity("REQ_2", "PATIENT_2", "HOSPITAL_1", "AMB_2", "ASSIGNED", "Road traffic injury", "8th Avenue", 40.7610, -73.9830, "HIGH", 9, now - 599_000, now - 159_000, null, false),
                EmergencyRequestEntity("REQ_3", "PATIENT_3", "HOSPITAL_2", null, "PENDING", "Breathing difficulty", "Wall Street", 40.7060, -74.0090, "HIGH", null, now - 598_000, now - 158_000, null, false),
                EmergencyRequestEntity("REQ_4", "PATIENT_4", "HOSPITAL_2", "AMB_3", "ARRIVED", "Fall from stairs", "Lexington Ave", 40.7510, -73.9730, "MEDIUM", 6, now - 597_000, now - 157_000, null, false),
                EmergencyRequestEntity("REQ_5", "PATIENT_1", "HOSPITAL_3", "AMB_5", "ASSIGNED", "Suspected stroke", "Broadway", 40.7240, -74.0000, "CRITICAL", 7, now - 596_000, now - 156_000, null, false),
                EmergencyRequestEntity("REQ_6", "PATIENT_2", "HOSPITAL_3", null, "PENDING", "Allergic reaction", "SoHo", 40.7230, -74.0025, "MEDIUM", null, now - 595_000, now - 155_000, null, false),
                EmergencyRequestEntity("REQ_7", "PATIENT_3", "HOSPITAL_4", "AMB_7", "ON_WAY", "Burn injury", "East Village", 40.7265, -73.9815, "HIGH", 5, now - 594_000, now - 154_000, null, false),
                EmergencyRequestEntity("REQ_8", "PATIENT_4", "HOSPITAL_4", "AMB_8", "COMPLETED", "Minor fracture", "Queens Blvd", 40.7440, -73.8900, "LOW", 12, now - 593_000, now - 153_000, now - 120_000, false),
                EmergencyRequestEntity("REQ_9", "PATIENT_1", "HOSPITAL_1", "AMB_1", "COMPLETED", "Fever and dehydration", "5th Avenue", 40.7750, -73.9650, "LOW", 10, now - 592_000, now - 152_000, now - 110_000, false),
                EmergencyRequestEntity("REQ_10", "PATIENT_2", "HOSPITAL_2", null, "PENDING", "Unconscious person", "Madison Ave", 40.7600, -73.9740, "CRITICAL", null, now - 591_000, now - 151_000, null, false)
            )

            requests.forEach { request ->
                if (requestDao.getRequestById(request.id) == null) {
                    requestDao.insertRequest(request)
                }
            }
        }
    }
}
