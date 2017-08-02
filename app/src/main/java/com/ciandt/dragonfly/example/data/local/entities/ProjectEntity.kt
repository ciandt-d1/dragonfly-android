package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
        @PrimaryKey(autoGenerate = false) var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: String = "",
        @Ignore var versions: List<VersionEntity> = emptyList(),
        var createdAt: Long = 0
)