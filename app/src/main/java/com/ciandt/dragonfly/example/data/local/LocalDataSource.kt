package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Room
import android.content.Context
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.config.Database as DatabaseConfig

class LocalDataSource(context: Context) {

    private val database by lazy {
        Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, DatabaseConfig.NAME)
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

    fun update(project: ProjectEntity) {
        versionDao.delete(project.id)
        save(project)
    }

    fun getProjects(): List<ProjectEntity> {
        val projects = arrayListOf<ProjectEntity>()

        projectDao.getProjects().forEach {
            it.map()?.let { mapped ->
                projects.add(mapped)
            }
        }

        return projects
    }

    fun clear() = database.runInTransaction {
        versionDao.clear()
        projectDao.clear()
    }
}