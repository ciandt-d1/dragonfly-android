package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ciandt.dragonfly.example.data.local.dao.ProjectDao
import com.ciandt.dragonfly.example.data.local.dao.VersionDao
import com.ciandt.dragonfly.example.data.local.entities.Project
import com.ciandt.dragonfly.example.data.local.entities.Version

@Database(entities = arrayOf(Project::class, Version::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao
}