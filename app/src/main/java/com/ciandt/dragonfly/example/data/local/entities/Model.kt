package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "models")
data class Model(
        @PrimaryKey(autoGenerate = false) var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: String = "",
        @Ignore var versions: List<Version> = emptyList()
)