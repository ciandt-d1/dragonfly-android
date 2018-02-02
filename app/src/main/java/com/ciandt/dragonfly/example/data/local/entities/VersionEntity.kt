package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index

@Entity(
        tableName = "versions",
        primaryKeys = arrayOf("project", "version"),
        indices = arrayOf(
                Index("status"),
                Index("created_at")
        )
)
data class VersionEntity(
        @ColumnInfo(name = "project")
        var project: String = "",

        @ColumnInfo(name = "version")
        var version: Int = 0,

        @ColumnInfo(name = "size")
        var size: Long = 0L,

        @ColumnInfo(name = "input_size")
        var inputSize: Int = 0,

        @ColumnInfo(name = "image_mean")
        var imageMean: Int = 0,

        @ColumnInfo(name = "image_std")
        var imageStd: Float = 0.0f,

        @ColumnInfo(name = "input_name")
        var inputName: String = "",

        @ColumnInfo(name = "output_names")
        var outputNames: String = "",

        @ColumnInfo(name = "download_url")
        var downloadUrl: String = "",

        @ColumnInfo(name = "created_at")
        var createdAt: Long = 0L,

        @ColumnInfo(name = "model_path")
        var modelPath: String = "",

        @ColumnInfo(name = "labels_files_paths")
        var labelFilesPaths: String = "",

        @ColumnInfo(name = "status")
        var status: Int = 0
)