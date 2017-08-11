package com.ciandt.dragonfly.example.features.feedback

import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.data.PendingFeedbackRepository
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.FirebaseDatabase

/**
 * Created by iluz on 6/23/17.
 */
class FeedbackSaverInteractor(database: FirebaseDatabase, val pendingFeedbackRepository: PendingFeedbackRepository) : FeedbackContract.SaverInteractor {
    private val databaseRef = database.reference
    private var onFeedbackSavedCallback: ((Feedback) -> Unit)? = null
    private var onFeedbackSaveErrorCallback: ((Feedback, Exception) -> Unit)? = null

    private val tasks = mutableListOf<SaveFeedbackTask>()

    override fun saveFeedback(feedback: Feedback) {
        val taskParams = SaveFeedbackTask.TaskParams(feedback)

        val task = SaveFeedbackTask(this)
        tasks.add(task)

        AsyncTaskCompat.executeParallel(task, taskParams)
    }

    override fun setOnFeedbackSavedCallback(callback: ((Feedback) -> Unit)?) {
        this.onFeedbackSavedCallback = callback
    }

    override fun setOnFeedbackSaveErrorCallback(callback: ((Feedback, Exception) -> Unit)?) {
        this.onFeedbackSaveErrorCallback = callback
    }

    companion object {
        private val LOG_TAG = FeedbackSaverInteractor::class.java.simpleName

        private class SaveFeedbackTask(val saverInteractor: FeedbackSaverInteractor) : AsyncTask<SaveFeedbackTask.TaskParams, Void, Void>() {

            override fun doInBackground(vararg params: TaskParams): Void? {
                val taskParams = params[0]

                try {
                    val feedbackKey = saverInteractor.databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).push().key
                    taskParams.feedback.key = feedbackKey

                    saverInteractor.databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).child(feedbackKey).setValue(taskParams.feedback)
                    saverInteractor.pendingFeedbackRepository.insert(taskParams.feedback)
                    saverInteractor.onFeedbackSavedCallback?.invoke(taskParams.feedback)
                } catch (e: Exception) {
                    DragonflyLogger.error(LOG_TAG, e)
                    saverInteractor.onFeedbackSaveErrorCallback?.invoke(taskParams.feedback, e)
                }

                return null
            }

            class TaskParams(val feedback: Feedback)
        }
    }
}