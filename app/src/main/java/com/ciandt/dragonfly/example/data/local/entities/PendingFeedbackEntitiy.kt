package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "pending_feedback",
        indices = arrayOf(Index("created_at"))
)
data class PendingFeedbackEntitiy(
        @PrimaryKey(autoGenerate = false)
        var id: String = "",

        @ColumnInfo(name = "tenant")
        var tenant: String = "",

        @ColumnInfo(name = "project")
        var project: String = "",

        @ColumnInfo(name = "user_id")
        var userId: String = "",

        @ColumnInfo(name = "model_version")
        var modelVersion: Int = 0,

        @ColumnInfo(name = "model_output_name")
        var modelOutputName: String = "",

        @ColumnInfo(name = "value")
        var value: Int = 0,

        @ColumnInfo(name = "actual_label")
        var actualLabel: String = "",

        @Ignore var identifiedLabels: List<PendingFeedbackLabelEntitiy> = emptyList(),

        @ColumnInfo(name = "image_local_path")
        var imageLocalPath: String = "",

        @ColumnInfo(name = "created_at")
        var createdAt: Long = 0
)