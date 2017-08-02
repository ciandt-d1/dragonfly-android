package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ciandt.dragonfly.example.data.local.dao.ProjectDao
import com.ciandt.dragonfly.example.data.local.dao.VersionDao
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity

@Database(entities = arrayOf(ProjectEntity::class, VersionEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao
}