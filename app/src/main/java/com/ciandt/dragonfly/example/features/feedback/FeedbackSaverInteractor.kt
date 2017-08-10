package com.ciandt.dragonfly.example.features.feedback

import android.os.AsyncTask
import android.support.v4.os.AsyncTaskCompat
import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.lastSegment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Created by iluz on 6/23/17.
 */
class FeedbackSaverSaverInteractor(storage: FirebaseStorage, database: FirebaseDatabase) : FeedbackContract.SaverInteractor {
    private val storageRef = storage.reference
    private val databaseRef = database.reference
    private var onFeedbackSavedCallback: ((Feedback) -> Unit)? = null
    private var onFeedbackUpdatedWithGcsLocationCallback: ((Feedback) -> Unit)? = null
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

    override fun setOnFeedbackUpdatedWithGcsLocationCallback(callback: ((Feedback) -> Unit)?) {
        this.onFeedbackUpdatedWithGcsLocationCallback = callback
    }

    companion object {
        private val LOG_TAG = FeedbackSaverSaverInteractor::class.java.simpleName

        private class SaveFeedbackTask(val saverInteractor: FeedbackSaverSaverInteractor) : AsyncTask<SaveFeedbackTask.TaskParams, Void, Void>() {

            override fun doInBackground(vararg params: TaskParams): Void? {
                val taskParams = params[0]

                DragonflyLogger.debug(LOG_TAG, "SaveFeedbackTask.doInBackground() - start")

                try {
                    val stream = FileInputStream(taskParams.feedback.imageLocalPath)
                    try {
                        val fileName = taskParams.feedback.imageLocalPath.lastSegment(File.separator)
                        val imageReference = saverInteractor.storageRef.child("${taskParams.feedback.tenant}/${taskParams.feedback.userId}/${fileName}")
                        val metadata = StorageMetadata.Builder()
                                .setContentType("image/jpeg")
                                .build()

                        val feedbackKey = if (taskParams.feedback.key != null)
                            taskParams.feedback.key
                        else
                            saverInteractor.databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).push().key

                        saverInteractor.databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).child(feedbackKey).setValue(taskParams.feedback)
                        saverInteractor.onFeedbackSavedCallback?.invoke(taskParams.feedback)

                        val uploadTask = imageReference.putStream(stream, metadata)
                        uploadTask.addOnFailureListener { exception ->
                            stream.close()

                            DragonflyLogger.error(LOG_TAG, "uploadTask - failure | error: ${exception.message}")

                            saverInteractor.onFeedbackSaveErrorCallback?.invoke(taskParams.feedback, exception)
                        }.addOnSuccessListener { taskSnapshot ->
                            stream.close()

                            taskSnapshot.metadata?.let {
                                val gcsPath = "${it.bucket}/${it.path}"

                                val updatedFeedback = taskParams.feedback.copy(
                                        imageGcsPath = gcsPath,
                                        uploadToGcsFinished = true
                                )

                                saverInteractor.databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).child(feedbackKey).setValue(updatedFeedback)

                                DragonflyLogger.debug(LOG_TAG, "uploadTask - success | feedback successfully updated with gcsPath: ${gcsPath}")

                                saverInteractor.onFeedbackUpdatedWithGcsLocationCallback?.invoke(updatedFeedback)
                            }
                        }.addOnProgressListener { taskSnapshot ->
                            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                            DragonflyLogger.debug(LOG_TAG, "Upload is $progress% done")
                        }.addOnPausedListener { taskSnapshot ->
                            DragonflyLogger.debug(LOG_TAG, "Upload is paused")
                        }
                    } catch (e: Exception) {
                        stream.close()

                        saverInteractor.onFeedbackSaveErrorCallback?.invoke(taskParams.feedback, e)
                    }
                } catch (e: FileNotFoundException) {
                    DragonflyLogger.warn(LOG_TAG, e.message!!)

                    saverInteractor.onFeedbackSaveErrorCallback?.invoke(taskParams.feedback, e)
                }

                return null
            }

            class TaskParams(val feedback: Feedback)
        }
    }
}