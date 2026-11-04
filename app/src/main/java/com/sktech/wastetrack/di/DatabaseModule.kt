package com.sktech.wastetrack.di

import android.content.Context
import androidx.room.Room
import com.sktech.wastetrack.data.local.db.WasteTrackDatabase
import com.sktech.wastetrack.data.local.db.dao.SyncQueueDao
import com.sktech.wastetrack.data.local.db.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WasteTrackDatabase {
        return Room.databaseBuilder(
            context,
            WasteTrackDatabase::class.java,
            "wastetrack_db"
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideUserDao(db: WasteTrackDatabase): UserDao = db.userDao()

    @Provides
    fun provideSyncQueueDao(db: WasteTrackDatabase): SyncQueueDao = db.syncQueueDao()
}
