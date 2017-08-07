package com.ciandt.dragonfly.example.data.local

import android.arch.persistence.room.Room
import android.content.Context
import com.ciandt.dragonfly.example.data.local.entities.DownloadEntity
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity
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

    private val downloadDao by lazy {
        database.getDownloadDao()
    }

    // PROJECTS
    private fun insert(project: ProjectEntity) {
        projectDao.insert(project)

        project.versions.forEach { version ->
            val downloadingOrDownloaded = (versionDao.downloadingOrDownloaded(version.project, version.version) != null)
            if (!downloadingOrDownloaded) {
                versionDao.insert(version)
            }
        }
    }

    fun save(project: ProjectEntity) = database.runInTransaction {
        insert(project)
    }

    fun delete(project: ProjectEntity) = database.runInTransaction {
        versionDao.delete(project.id)
        projectDao.delete(project)
    }

    fun update(project: ProjectEntity) = database.runInTransaction {
        versionDao.delete(project.id)
        insert(project)
    }

    fun getProject(id: String): ProjectEntity? {
        return projectDao.getProject(id)?.map()
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

    // VERSIONS
    fun updateVersion(version: VersionEntity) = database.runInTransaction {
        versionDao.insert(version)
    }

    fun updateVersionStatus(project: String, version: Int, status: Int) {
        versionDao.updateStatus(project, version, status)
    }

    fun getVersionByDownload(id: Long): VersionEntity? {
        return versionDao.getByDownload(id)
    }

    // DOWNLOADS
    fun saveDownload(id: Long, project: String, version: Int) {
        downloadDao.insert(DownloadEntity(id, project, version))
    }

    fun deleteDownload(id: Long) {
        downloadDao.delete(id)
    }

    // CLEAR
    fun clear() = database.runInTransaction {
        versionDao.clear()
        projectDao.clear()
        downloadDao.clear()
    }
}