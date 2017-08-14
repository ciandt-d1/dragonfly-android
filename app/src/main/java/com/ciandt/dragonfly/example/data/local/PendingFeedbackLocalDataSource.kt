package com.ciandt.dragonfly.example.data.local

import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.config.Database as DatabaseConfig

class PendingFeedbackLocalDataSource(val database: AppDatabase) {
    private val pendingFeedbackDao by lazy {
        database.getPendingFeedbackDao()
    }

    private val pendingFeedbackLabelDao by lazy {
        database.getPendingeFeedbackLabelDao()
    }

    fun insert(feedback: PendingFeedbackEntitiy) = database.runInTransaction {
        pendingFeedbackDao.insert(feedback)

        feedback.identifiedLabels.forEach { label ->
            pendingFeedbackLabelDao.insert(label)
        }
    }

    fun delete(id: String) = database.runInTransaction {
        pendingFeedbackLabelDao.delete(id)
        pendingFeedbackDao.delete(id)
    }

    fun getLimitingTo(limit: Int): List<PendingFeedbackEntitiy> {
        val feedbackList = arrayListOf<PendingFeedbackEntitiy>()

        pendingFeedbackDao.getLimitingTo(limit).forEach {
            it.map()?.let { mapped ->
                feedbackList.add(mapped)
            }
        }

        return feedbackList
    }

    fun clear() = database.runInTransaction {
        pendingFeedbackLabelDao.clear()
        pendingFeedbackDao.clear()
    }
}