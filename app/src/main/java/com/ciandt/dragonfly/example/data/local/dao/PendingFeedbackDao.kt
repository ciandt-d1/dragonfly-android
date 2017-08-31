package com.ciandt.dragonfly.example.data.local.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.relations.PendingFeedbackWithLabels

@Dao
interface PendingFeedbackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(feedback: PendingFeedbackEntitiy)

    @Query("DELETE " +
            "FROM pending_feedback " +
            "WHERE id = :arg0")
    fun delete(id: String)

    @Query("SELECT * " +
            "FROM pending_feedback " +
            "ORDER BY created_at ASC " +
            "LIMIT :arg0")
    fun getLimitingTo(limit: Int): List<PendingFeedbackWithLabels>

    @Query("DELETE " +
            "FROM pending_feedback")
    fun clear()
}