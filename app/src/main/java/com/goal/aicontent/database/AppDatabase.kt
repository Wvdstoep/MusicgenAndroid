package com.goal.aicontent.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadableContentEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadableContentDao(): DownloadableContentDao
}
