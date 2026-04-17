package com.example.mobiledev.data.local.dao

import androidx.room.*
import com.example.mobiledev.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM USER WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM USER WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM USER")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM USER WHERE user_type = :type")
    fun getUsersByType(type: String): Flow<List<UserEntity>>
}
