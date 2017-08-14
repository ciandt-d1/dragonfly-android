package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy

@Dao
interface PendingFeedbackLabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(label: PendingFeedbackLabelEntitiy)

    @Query("DELETE FROM pending_feedback_label WHERE feedback_id = :arg0")
    fun delete(feedbackId: String)

    @Query("DELETE FROM pending_feedback_label")
    fun clear()
}