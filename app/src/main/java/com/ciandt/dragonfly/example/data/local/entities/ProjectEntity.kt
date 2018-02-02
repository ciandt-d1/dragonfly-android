package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "projects",
        indices = arrayOf(Index("created_at"))
)
data class ProjectEntity(
        @PrimaryKey(autoGenerate = false)
        var id: String = "",

        @ColumnInfo(name = "name")
        var name: String = "",

        @ColumnInfo(name = "description")
        var description: String = "",

        @ColumnInfo(name = "colors")
        var colors: String = "",

        @Ignore var versions: List<VersionEntity> = emptyList(),

        @ColumnInfo(name = "created_at")
        var createdAt: Long = 0,

        @ColumnInfo(name = "show_benchmark")
        var showBenchmark: Boolean = false
)