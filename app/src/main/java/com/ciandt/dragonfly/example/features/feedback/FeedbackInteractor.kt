package com.ciandt.dragonfly.example.features.feedback

import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.lastChunk
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File
import java.io.FileInputStream


/**
 * Created by iluz on 6/23/17.
 */
class FeedbackInteractor(storage: FirebaseStorage, database: FirebaseDatabase) : FeedbackContract.Interactor {
    private val storageRef = storage.reference
    private val databaseRef = database.reference
    private var onFeedbackSavedCallback: ((Feedback) -> Unit)? = null

    override fun saveFeedback(feedback: Feedback) {
        val taskParams = SaveFeedbackTask.TaskParams(feedback)
        AsyncTaskCompat.executeParallel(SaveFeedbackTask(this), taskParams)
    }

    override fun setOnFeedbackSavedCallback(callback: ((Feedback) -> Unit)?) {
        this.onFeedbackSavedCallback = callback
    }

    companion object {
        private val LOG_TAG = FeedbackInteractor::class.java.simpleName

        private val FEEDBACK_COLLECTION = "feedback_stash"

        private class SaveFeedbackTask(interactor: FeedbackInteractor) : AsyncTask<SaveFeedbackTask.TaskParams, Void, Void>() {
            val interactor = interactor

            override fun doInBackground(vararg params: TaskParams): Void? {
                val taskParams = params[0]

                DragonflyLogger.debug(LOG_TAG, "SaveFeedbackTask.doInBackground() - start")

                val stream = FileInputStream(taskParams.feedback.imageLocalPath)
                try {
                    val fileName = taskParams.feedback.imageLocalPath.lastChunk(File.separator)
                    val imageReference = interactor.storageRef.child("${taskParams.feedback.tenant}/${taskParams.feedback.userId}/${fileName}")
                    val metadata = StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build()

                    val feedbackKey = interactor.databaseRef.child(FEEDBACK_COLLECTION).push().getKey()

                    val uploadTask = imageReference.putStream(stream, metadata)
                    uploadTask.addOnFailureListener({ exception ->
                        stream.close()

                        DragonflyLogger.debug(LOG_TAG, "SaveFeedbackTask.doInBackground() - failure | downloadUrl: ${exception.message}")

                        interactor.databaseRef.child(FEEDBACK_COLLECTION).child(feedbackKey).setValue(taskParams.feedback)

                        interactor.onFeedbackSavedCallback?.invoke(taskParams.feedback)
                    }).addOnSuccessListener({ taskSnapshot ->
                        stream.close()

                        if (taskSnapshot.metadata != null) {
                            val metadata = taskSnapshot.metadata!!
                            val gcsPath = "${metadata.bucket}/${metadata.path}"

                            DragonflyLogger.debug(LOG_TAG, "SaveFeedbackTask.doInBackground() - success | gcsPath: ${gcsPath}")

                            val updatedFeedback = taskParams.feedback.copy(
                                    imageGcsPath = gcsPath,
                                    uploadToGcsFinished = true
                            )

                            interactor.databaseRef.child(FEEDBACK_COLLECTION).child(feedbackKey).setValue(updatedFeedback)

                            interactor.onFeedbackSavedCallback?.invoke(updatedFeedback)
                        } else {
                            interactor.onFeedbackSavedCallback?.invoke(taskParams.feedback)
                        }
                    })
                } catch(e: Exception) {
                    stream.close()
                }

                return null
            }

            class TaskParams(val feedback: Feedback)
        }
    }
}