package com.ciandt.dragonfly.example.data.local.relations

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity
import com.ciandt.dragonfly.example.data.mapper.Mapper

class ProjectVersions : Mapper<ProjectEntity>() {

    @Embedded
    var project: ProjectEntity? = null

    @Relation(parentColumn = "id", entityColumn = "project", entity = VersionEntity::class)
    var versions: List<VersionEntity> = emptyList()

    override fun map(): ProjectEntity? {

        project?.let {
            it.versions = versions
            return it
        }

        return project
    }
}