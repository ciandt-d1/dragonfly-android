package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.ProjectEntity
import com.ciandt.dragonfly.example.data.local.relations.ProjectVersions

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(project: ProjectEntity)

    @Delete
    fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    fun getProjects(): List<ProjectVersions>

    @Query("DELETE FROM projects")
    fun clear()
}