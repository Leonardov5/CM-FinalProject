package com.example.finalproject.data.sync

import android.content.Context
import com.example.finalproject.data.local.AppDatabase
import com.example.finalproject.data.local.LocalUser
import com.example.finalproject.data.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserSyncManager {

    suspend fun syncUserDataWithConflictResolution(context: Context) {
        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()

        withContext(Dispatchers.IO) {
            try {
                val remoteUser = UserService.getCurrentUserData()
                val localUser = userDao.getUserById(remoteUser?.id ?: "")

                if (remoteUser != null) {
                    if (localUser == null || remoteUser.updatedAt > localUser.updatedAt) {
                        val newLocalUser = LocalUser(
                            id = remoteUser.id,
                            username = remoteUser.username,
                            nome = remoteUser.nome,
                            email = remoteUser.email ?: "",
                            fotografia = remoteUser.fotografia,
                            updatedAt = remoteUser.updatedAt
                        )
                        userDao.insertUser(newLocalUser)
                    } else if (localUser.updatedAt > remoteUser.updatedAt) {
                        UserService.updateUserData(
                            username = localUser.username,
                            nome = localUser.nome,
                            fotografia = localUser.fotografia
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncUserData(context: Context) {
        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()

        withContext(Dispatchers.IO) {
            try {
                val remoteUser = UserService.getCurrentUserData()
                if (remoteUser != null) {
                    val localUser = LocalUser(
                        id = remoteUser.id,
                        username = remoteUser.username,
                        nome = remoteUser.nome,
                        email = remoteUser.email ?: "",
                        fotografia = remoteUser.fotografia,
                        updatedAt = remoteUser.updatedAt
                    )
                    userDao.insertUser(localUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun pushLocalChanges(context: Context) {
        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()

        withContext(Dispatchers.IO) {
            try {
                val userId = UserService.getCurrentUserData()?.id ?: return@withContext
                val localUser = userDao.getUserById(userId)

                if (localUser != null) {
                    UserService.updateUserData(
                        username = localUser.username,
                        nome = localUser.nome,
                        fotografia = localUser.fotografia
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveUserDataLocally(context: Context) {
        val db = AppDatabase.getInstance(context)
        val userDao = db.userDao()

        withContext(Dispatchers.IO) {
            try {
                val remoteUser = UserService.getCurrentUserData()
                if (remoteUser != null) {
                    val localUser = LocalUser(
                        id = remoteUser.id,
                        username = remoteUser.username,
                        nome = remoteUser.nome,
                        email = remoteUser.email ?: "",
                        fotografia = remoteUser.fotografia,
                        updatedAt = remoteUser.updatedAt
                    )
                    userDao.insertUser(localUser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}