package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.entities.VersionEntity
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.DataSnapshot

class DataSnapshotToProjectEntityMapper(val dataSnapshot: DataSnapshot) : Mapper<ProjectEntity>() {

    override fun map(): ProjectEntity? {

        listOf("name", "description", "colors", "createdAt").forEach {
            if (!dataSnapshot.hasChild(it)) {
                DragonflyLogger.error(LOG_TAG, IllegalArgumentException("child '$it' not found"))
                return null
            }
        }

        try {
            val colors = ArrayList<String>()
            dataSnapshot.child("colors").children.forEach {
                colors.add(it.getValue(String::class.java)!!)
            }

            val versions = ArrayList<VersionEntity>()
            dataSnapshot.child("versions").children.forEach { child ->
                mapVersion(dataSnapshot.key, child)?.let { version ->
                    versions.add(version)
                }
            }

            return ProjectEntity(
                    id = dataSnapshot.key,
                    name = dataSnapshot.child("name").getValue(String::class.java)!!,
                    description = dataSnapshot.child("description").getValue(String::class.java)!!,
                    createdAt = dataSnapshot.child("createdAt").getValue(Long::class.java)!!,
                    colors = colors.joinToString(","),
                    versions = versions
            )

        } catch (e: Exception) {
            DragonflyLogger.error(LOG_TAG, e.message, e)
            return null
        }
    }

    private fun mapVersion(project: String, dataSnapshot: DataSnapshot): VersionEntity? {
        try {
            val version = dataSnapshot.getValue(VersionEntity::class.java)!!
            version.project = project
            return version

        } catch (e: Exception) {
            DragonflyLogger.error(LOG_TAG, e.message, e)
            return null
        }
    }

    companion object {
        private val LOG_TAG = DataSnapshotToProjectEntityMapper::class.java.simpleName
    }
}