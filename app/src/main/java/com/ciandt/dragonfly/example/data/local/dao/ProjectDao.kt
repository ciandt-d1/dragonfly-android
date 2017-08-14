package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.*
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.relations.ProjectVersions

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(project: ProjectEntity)

    @Delete
    fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects WHERE id = :arg0")
    fun getProject(id: String): ProjectVersions?

    @Query("SELECT * FROM projects ORDER BY created_at ASC")
    fun getProjects(): List<ProjectVersions>

    @Query("DELETE FROM projects")
    fun clear()
}