package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.models.Project
import com.ciandt.dragonfly.example.models.Version

class ProjectEntityToProjectMapper(val entity: ProjectEntity) : Mapper<Project>() {

    override fun map(): Project? = with(entity) {

        val versionsMapped = arrayListOf<Version>()

        versions.forEach {
            VersionEntityToVersionMapper(it).map()?.let { mapped ->
                versionsMapped.add(mapped)
            }
        }

        return Project(
                id,
                name,
                description,
                colors.split(","),
                versionsMapped
        )
    }
}