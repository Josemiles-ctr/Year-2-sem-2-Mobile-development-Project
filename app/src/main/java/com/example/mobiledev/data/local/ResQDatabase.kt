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
    version = 4, // Bumped version to ensure schema refresh
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

            // Hash the default password for the seed data
            val hashedPW = BCrypt.hashpw("password123", BCrypt.gensalt())

            // 1. Seed Hospital (Check if it exists to make seeding idempotent)
            val existingHospital = hospitalDao.getHospitalById("HOSPITAL_1")
            if (existingHospital == null) {
                val hospital = HospitalEntity(
                    id = "HOSPITAL_1",
                    adminId = "ADMIN_1",
                    name = "City Central Hospital",
                    email = "contact@cityhospital.com",
                    phone = "555-0100",
                    location = "123 Healthcare Ave",
                    latitude = 40.7128,
                    longitude = -74.0060,
                    uuid = "UUID_1",
                    passwordHash = hashedPW,
                    status = HospitalStatus.APPROVED,
                    activeAmbulances = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                hospitalDao.insertHospital(hospital)
            }

            // 2. Seed Users (Drivers link to Hospital)
            if (userDao.getUserById("USER_1") == null) {
                val user1 = UserEntity(
                    id = "USER_1",
                    hospitalId = null,
                    name = "John Doe",
                    email = "john@example.com",
                    phone = "123456789",
                    location = "User Location 1",
                    userType = "PATIENT",
                    uuid = "UUID_USER_1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(user1)
            }
            
            if (userDao.getUserById("USER_2") == null) {
                val user2 = UserEntity(
                    id = "USER_2",
                    hospitalId = null,
                    name = "Jane Smith",
                    email = "jane@example.com",
                    phone = "987654321",
                    location = "User Location 2",
                    userType = "PATIENT",
                    uuid = "UUID_USER_2",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(user2)
            }

            if (userDao.getUserById("DRIVER_1") == null) {
                val driver1 = UserEntity(
                    id = "DRIVER_1",
                    hospitalId = "HOSPITAL_1",
                    name = "Driver One",
                    email = "driver1@example.com",
                    phone = "555-0001",
                    location = "Ambulance Station",
                    userType = "DRIVER",
                    uuid = "UUID_DRIVER_1",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                userDao.insertUser(driver1)
            }

            // 3. Seed Ambulances (Link to Hospital and Driver)
            if (ambulanceDao.getAmbulanceById("AMB_1") == null) {
                val ambulance1 = AmbulanceEntity(
                    id = "AMB_1",
                    hospitalId = "HOSPITAL_1",
                    driverId = "DRIVER_1",
                    registrationNo = "NYC-001",
                    licenseNo = "LIC-999",
                    status = "AVAILABLE",
                    latitude = 40.7130,
                    longitude = -74.0065,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                ambulanceDao.insertAmbulance(ambulance1)
            }

            // 4. Seed Mock Requests
            if (requestDao.getRequestById("REQ_1") == null) {
                val request1 = EmergencyRequestEntity(
                    id = "REQ_1",
                    userId = "USER_1",
                    hospitalId = "HOSPITAL_1",
                    ambulanceId = null,
                    status = "PENDING",
                    description = "Severe chest pain, possible heart attack",
                    location = "Times Square, NY",
                    latitude = 40.7580,
                    longitude = -73.9855,
                    priority = "CRITICAL",
                    estimatedTimeMins = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    completedAt = null,
                    isDeleted = false
                )
                requestDao.insertRequest(request1)
            }
            
            if (requestDao.getRequestById("REQ_2") == null) {
                val request2 = EmergencyRequestEntity(
                    id = "REQ_2",
                    userId = "USER_2",
                    hospitalId = "HOSPITAL_1",
                    ambulanceId = null,
                    status = "PENDING",
                    description = "Minor car accident, one injury",
                    location = "Central Park West",
                    latitude = 40.7812,
                    longitude = -73.9665,
                    priority = "MEDIUM",
                    estimatedTimeMins = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    completedAt = null,
                    isDeleted = false
                )
                requestDao.insertRequest(request2)
            }
        }
    }
}
