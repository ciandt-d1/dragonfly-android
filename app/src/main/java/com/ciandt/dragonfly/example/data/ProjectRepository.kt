package com.ciandt.dragonfly.example.data

import com.ciandt.dragonfly.example.config.Features
import com.ciandt.dragonfly.example.data.local.AppDatabase
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.ciandt.dragonfly.example.data.mapper.ProjectEntityToProjectMapper
import com.ciandt.dragonfly.example.data.mapper.VersionEntityToVersionMapper
import com.ciandt.dragonfly.example.data.mapper.VersionToVersionEntityMapper
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

class ProjectRepository(database: AppDatabase) {

    private val localDataSource = LocalDataSource(database)

    fun getProject(id: String): Project? {
        localDataSource.getProject(id)?.let {
            return ProjectEntityToProjectMapper(it).map()
        }
        return null
    }

    fun getProjects(): List<Project> {
        val projects = arrayListOf<Project>()

        val showProjectsWithoutVersions = Features.SHOW_PROJECTS_WITHOUT_VERSIONS

        localDataSource.getProjects().forEach {
            ProjectEntityToProjectMapper(it).map()?.let { mapped ->
                if (mapped.versions.isNotEmpty() || showProjectsWithoutVersions) {
                    projects.add(mapped)
                }
            }
        }

        return projects
    }

    fun updateVersion(version: Version) {
        localDataSource.updateVersion(VersionToVersionEntityMapper(version).map())
    }

    fun updateVersionStatus(project: String, version: Int, status: Int) {
        localDataSource.updateVersionStatus(project, version, status)
    }

    fun getVersionByDownload(id: Long): Version? {
        localDataSource.getVersionByDownload(id)?.let {
            return VersionEntityToVersionMapper(it).map()
        }
        return null
    }

    fun saveDownload(id: Long, project: String, version: Int) {
        localDataSource.saveDownload(id, project, version)
    }

    fun deleteDownload(id: Long) {
        localDataSource.deleteDownload(id)
    }

    fun clear() {
        localDataSource.clear()
    }
}