package com.ciandt.dragonfly.example.data.local.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(
        tableName = "pending_feedback_label",
        indices = arrayOf(Index("feedback_id"))
)
data class PendingFeedbackLabelEntitiy(
        @PrimaryKey(autoGenerate = true)
        var id: Long? = null,

        @ColumnInfo(name = "feedback_id")
        var feedbackId: String = "",

        @ColumnInfo(name = "label")
        var label: String = "",

        @ColumnInfo(name = "confidence")
        var confidence: Float = 0f
)