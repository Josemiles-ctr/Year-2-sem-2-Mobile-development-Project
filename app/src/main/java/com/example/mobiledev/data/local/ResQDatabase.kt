package com.example.mobiledev.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
 import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledev.data.local.dao.AmbulanceDao
import com.example.mobiledev.data.local.dao.EmergencyRequestDao
import com.example.mobiledev.data.local.dao.HospitalDao
import com.example.mobiledev.data.local.dao.UserDao
import com.example.mobiledev.data.local.entity.AmbulanceEntity
import com.example.mobiledev.data.local.entity.EmergencyRequestEntity
import com.example.mobiledev.data.local.entity.HospitalEntity
import com.example.mobiledev.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        HospitalEntity::class,
        AmbulanceEntity::class,
        EmergencyRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ResQDatabase : RoomDatabase() {

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

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val currentTime = System.currentTimeMillis()
                
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    val hospitalDao = database.hospitalDao()
                    val ambulanceDao = database.ambulanceDao()
                    val userDao = database.userDao()

                    // Seed Hospitals
                    val mulagoId = UUID.randomUUID().toString()
                    val nsambyaId = UUID.randomUUID().toString()
                    
                    hospitalDao.insertHospital(HospitalEntity(
                        id = mulagoId,
                        adminId = "admin1",
                        name = "Mulago Hospital",
                        email = "info@mulago.ug",
                        phone = "+256414554001",
                        location = "Mulago Hill, Kampala",
                        latitude = 0.3476,
                        longitude = 32.5825,
                        uuid = UUID.randomUUID().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))

                    hospitalDao.insertHospital(HospitalEntity(
                        id = nsambyaId,
                        adminId = "admin2",
                        name = "Nsambya Hospital",
                        email = "info@nsambyahospital.or.ug",
                        phone = "+256414267012",
                        location = "Nsambya, Kampala",
                        latitude = 0.3031,
                        longitude = 32.5811,
                        uuid = UUID.randomUUID().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))

                    // Seed Drivers
                    val driver1Id = UUID.randomUUID().toString()
                    val driver2Id = UUID.randomUUID().toString()

                    userDao.insertUser(UserEntity(
                        id = driver1Id,
                        hospitalId = mulagoId,
                        name = "John Driver",
                        email = "john@resq.com",
                        phone = "0700000001",
                        location = "Kampala",
                        userType = "DRIVER",
                        uuid = UUID.randomUUID().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))

                    userDao.insertUser(UserEntity(
                        id = driver2Id,
                        hospitalId = nsambyaId,
                        name = "Jane Driver",
                        email = "jane@resq.com",
                        phone = "0700000002",
                        location = "Kampala",
                        userType = "DRIVER",
                        uuid = UUID.randomUUID().toString(),
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))

                    // Seed Ambulances
                    ambulanceDao.insertAmbulance(AmbulanceEntity(
                        id = UUID.randomUUID().toString(),
                        hospitalId = mulagoId,
                        driverId = driver1Id,
                        registrationNo = "MA-202",
                        licenseNo = "L-202",
                        status = "AVAILABLE",
                        latitude = 0.3400,
                        longitude = 32.5750,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))

                    ambulanceDao.insertAmbulance(AmbulanceEntity(
                        id = UUID.randomUUID().toString(),
                        hospitalId = nsambyaId,
                        driverId = driver2Id,
                        registrationNo = "MA-305",
                        licenseNo = "L-305",
                        status = "AVAILABLE",
                        latitude = 0.3550,
                        longitude = 32.5900,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    ))
                }
            }
        }
    }
}
