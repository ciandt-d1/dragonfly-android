package com.ciandt.dragonfly.example.data

import com.ciandt.dragonfly.example.data.local.AppDatabase
import com.ciandt.dragonfly.example.data.local.PendingFeedbackLocalDataSource
import com.ciandt.dragonfly.example.data.mapper.FeedbackToPendingFeedbackEntityMapper
import com.ciandt.dragonfly.example.data.mapper.PendingFeedbackEntityToFeedbackMapper
import com.ciandt.dragonfly.example.features.feedback.model.Feedback

class PendingFeedbackRepository(database: AppDatabase) {

    private val localDataSource = PendingFeedbackLocalDataSource(database)

    fun insert(feedback: Feedback) {
        localDataSource.insert(FeedbackToPendingFeedbackEntityMapper(feedback).map())
    }

    fun delete(id: String) = localDataSource.delete(id)

    fun getLimitingTo(limit: Int): List<Feedback> {
        val pendingFeedbackList = arrayListOf<Feedback>()

        localDataSource.getLimitingTo(limit).forEach {
            PendingFeedbackEntityToFeedbackMapper(it).map()?.let { mapped ->
                pendingFeedbackList.add(mapped)
            }
        }

        return pendingFeedbackList
    }

    fun clear() = localDataSource.clear()
}