package com.sktech.wastetrack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sktech.wastetrack.data.local.db.dao.SyncQueueDao
import com.sktech.wastetrack.data.local.db.dao.UserDao
import com.sktech.wastetrack.data.local.db.entity.SyncQueueEntity
import com.sktech.wastetrack.data.local.db.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SyncQueueEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WasteTrackDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun syncQueueDao(): SyncQueueDao

    suspend fun clearUserScopedData() {
        userDao().deleteAll()
    }
}
