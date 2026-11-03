package com.sktech.wastetrack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sktech.wastetrack.data.local.db.dao.UserDao
import com.sktech.wastetrack.data.local.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WasteTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    suspend fun clearUserScopedData() {
        userDao().deleteAll()
    }
}
