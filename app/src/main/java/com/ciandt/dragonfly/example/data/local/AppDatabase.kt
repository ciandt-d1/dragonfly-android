package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ciandt.dragonfly.example.data.local.dao.ModelDao
import com.ciandt.dragonfly.example.data.local.dao.VersionDao
import com.ciandt.dragonfly.example.data.local.entities.Model
import com.ciandt.dragonfly.example.data.local.entities.Version

@Database(entities = arrayOf(Model::class, Version::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getModelDao(): ModelDao

    abstract fun getVersionDao(): VersionDao
}