package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ciandt.dragonfly.example.data.local.dao.DownloadDao
import com.ciandt.dragonfly.example.data.local.dao.PendingFeedbackDao
import com.ciandt.dragonfly.example.data.local.dao.PendingFeedbackLabelDao
import com.ciandt.dragonfly.example.data.local.dao.ProjectDao
import com.ciandt.dragonfly.example.data.local.dao.VersionDao
import com.ciandt.dragonfly.example.data.local.entities.DownloadEntity
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity

@Database(
        entities = arrayOf(
                ProjectEntity::class,
                VersionEntity::class,
                DownloadEntity::class,
                PendingFeedbackEntitiy::class,
                PendingFeedbackLabelEntitiy::class
        ),
        version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao

    abstract fun getDownloadDao(): DownloadDao

    abstract fun getPendingFeedbackDao(): PendingFeedbackDao

    abstract fun getPendingFeedbackLabelDao(): PendingFeedbackLabelDao
}