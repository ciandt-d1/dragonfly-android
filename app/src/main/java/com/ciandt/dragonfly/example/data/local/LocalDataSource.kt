package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Room
import android.content.Context
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity

class LocalDataSource(context: Context) {

    private val database by lazy {
        Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, BuildConfig.APPLICATION_ID)
                .fallbackToDestructiveMigration()
                .build()
    }

    private val projectDao by lazy {
        database.getProjectDao()
    }

    private val versionDao by lazy {
        database.getVersionDao()
    }

    fun save(project: ProjectEntity) = database.runInTransaction {

        projectDao.insert(project)

        project.versions.forEach { version ->
            val downloadingOrDownloaded = (versionDao.downloadingOrDownloaded(version.project, version.version) != null)
            if (!downloadingOrDownloaded) {
                versionDao.insert(version)
            }
        }
    }

    fun delete(project: ProjectEntity) = database.runInTransaction {
        versionDao.delete(project.id)
        projectDao.delete(project)
    }

    fun clear() = database.runInTransaction {
        versionDao.clear()
        projectDao.clear()
    }
}