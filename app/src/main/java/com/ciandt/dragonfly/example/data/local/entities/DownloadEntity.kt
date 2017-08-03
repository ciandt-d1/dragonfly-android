package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
        @PrimaryKey(autoGenerate = false) var id: Long = -1,
        var project: String = "",
        var version: Int = -1
)