package com.example.mobiledev.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobiledev.data.mock.MockSeedData
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

            val mockSeed = MockSeedData.create(now = now, passwordHash = hashedPW)

            mockSeed.hospitals.forEach { hospital ->
                if (hospitalDao.getHospitalById(hospital.id) == null) {
                    hospitalDao.insertHospital(hospital)
                }
            }

            mockSeed.users.forEach { user ->
                if (userDao.getUserById(user.id) == null) {
                    userDao.insertUser(user)
                }
            }

            mockSeed.ambulances.forEach { ambulance ->
                if (ambulanceDao.getAmbulanceById(ambulance.id) == null) {
                    ambulanceDao.insertAmbulance(ambulance)
                }
            }

            mockSeed.requests.forEach { request ->
                if (requestDao.getRequestById(request.id) == null) {
                    requestDao.insertRequest(request)
                }
            }
        }
    }
}
