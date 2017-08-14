package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ciandt.dragonfly.example.data.local.dao.*
import com.ciandt.dragonfly.example.data.local.entities.*

@Database(
        entities = arrayOf(
                ProjectEntity::class,
                VersionEntity::class,
                DownloadEntity::class,
                PendingFeedbackEntitiy::class,
                PendingFeedbackLabelEntitiy::class
        ),
        version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao

    abstract fun getDownloadDao(): DownloadDao

    abstract fun getPendingFeedbackDao(): PendingFeedbackDao

    abstract fun getPendingeFeedbackLabelDao(): PendingFeedbackLabelDao
}