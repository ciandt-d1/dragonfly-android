package com.ciandt.dragonfly.example.data

import android.content.Context
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.ciandt.dragonfly.example.data.mapper.ProjectEntityToProjectMapper
import com.ciandt.dragonfly.example.data.mapper.VersionEntityToVersionMapper
import com.ciandt.dragonfly.example.data.mapper.VersionToVersionEntityMapper
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

class ProjectRepository(context: Context) {

    private val localDataSource = LocalDataSource(context)

    fun getProject(id: String): Project? {
        localDataSource.getProject(id)?.let {
            return ProjectEntityToProjectMapper(it).map()
        }
        return null
    }

    fun getProjects(): List<Project> {

        val projects = arrayListOf<Project>()

//        localDataSource.getProjects().forEach {
//            ProjectEntityToProjectMapper(it).map()?.let { mapped ->
//                projects.add(mapped)
//            }
//        }
        val project = Project(
                "flowers",
                "Flowers",
                "This model classifies 130 plants",
                listOf("#9BCE4F", "#228B22")
        )

        val version = Version(
                "flowers",
                1,
                88481067L,
                224,
                128,
                128f,
                "Mul",
                "final_ops/softmax",
                "",
                1L,
                "file:///android_asset/models/flowers/inception_v1_quantized_optimized_frozen_graph_PL.pb",
                "file:///android_asset/models/flowers/dict.txt",
                Version.STATUS_DOWNLOADED
        )

        project.versions = listOf(version)

        projects.add(project)

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