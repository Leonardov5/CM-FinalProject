package com.example.finalproject.data.local

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): LocalUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LocalUser)

    @Update
    suspend fun updateUser(user: LocalUser)

    @Transaction
    suspend fun insertOrUpdate(user: LocalUser) {
        val existingUser = getUserById(user.id)
        if (existingUser == null) {
            insertUser(user)
        } else {
            updateUser(user)
        }
    }
}