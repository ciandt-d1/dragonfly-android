package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "downloads",
        indices = arrayOf(Index("version"))
)
data class DownloadEntity(
        @PrimaryKey(autoGenerate = false)
        var id: Long = -1,

        @ColumnInfo(name = "project")
        var project: String = "",

        @ColumnInfo(name = "version")
        var version: Int = -1
)