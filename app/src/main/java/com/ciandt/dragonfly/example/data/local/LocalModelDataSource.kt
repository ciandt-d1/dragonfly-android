package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Room
import android.content.Context
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.data.local.entities.Model

class LocalModelDataSource(context: Context) {

    private val database by lazy {
        Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, BuildConfig.APPLICATION_ID)
                .fallbackToDestructiveMigration()
                .build()
    }

    private val modelDao by lazy {
        database.getModelDao()
    }

    fun save(model: Model) = database.runInTransaction {
        modelDao.insert(model)
    }
}