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

    private val versionDao by lazy {
        database.getVersionDao()
    }

    fun save(model: Model) = database.runInTransaction {

        modelDao.insert(model)

        model.versions.forEach {
            with(it) {
                val downloadingOrDownloaded = (versionDao.downloadingOrDownloaded(idModel, version) != null)
                if (!downloadingOrDownloaded) {
                    versionDao.insert(it)
                }
            }
        }

    }

    fun clear() = database.runInTransaction {
        versionDao.clear()
        modelDao.clear()
    }
}