package com.ciandt.dragonfly.example.data

import android.content.Context
import com.ciandt.dragonfly.example.data.local.LocalDataSource
import com.ciandt.dragonfly.example.data.mapper.ProjectEntityToProjectMapper
import com.ciandt.dragonfly.example.models.Project

class ProjectRepository(context: Context) {

    private val localDataSource = LocalDataSource(context)

    fun getProjects(): List<Project> {

        val projects = arrayListOf<Project>()

        localDataSource.getProjects().forEach {
            ProjectEntityToProjectMapper(it).map()?.let { mapped ->
                projects.add(mapped)
            }
        }

        return projects
    }

    fun clear() {
        localDataSource.clear()
    }
}