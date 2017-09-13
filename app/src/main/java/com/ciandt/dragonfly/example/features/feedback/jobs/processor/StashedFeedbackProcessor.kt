package com.ciandt.dragonfly.example.features.feedback.jobs.processor

import android.os.Looper
import android.os.NetworkOnMainThreadException
import android.support.annotation.WorkerThread
import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.data.PendingFeedbackRepository
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.lastSegment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


/**
 * Created by iluz on 8/4/17.
 */
class StashedFeedbackProcessor(database: FirebaseDatabase, storage: FirebaseStorage, val pendingFeedbackRepository: PendingFeedbackRepository, val limit: Int) : Processor {
    private val databaseRef = database.reference
    private val storageRef = storage.reference

    @WorkerThread
    override fun process() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw NetworkOnMainThreadException()
        }

        val pendingFeedbackList = pendingFeedbackRepository.getLimitingTo(limit)
        if (pendingFeedbackList.isEmpty()) {
            DragonflyLogger.info(LOG_TAG, "No pending feedbacks found.")
            return
        }

        val itemsCountDownLatch = CountDownLatch(pendingFeedbackList.size)
        val executor = Executors.newSingleThreadExecutor()

        pendingFeedbackList.forEach { feedback ->
            try {
                val stream = FileInputStream(feedback.imageLocalPath)
                try {
                    val fileName = feedback.imageLocalPath.lastSegment(File.separator)
                    val imageReference = storageRef.child("${feedback.tenant}/${feedback.userId}/${fileName}")
                    val metadata = StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build()

                    imageReference.putStream(stream, metadata)
                            .addOnFailureListener(executor, OnFailureListener { exception ->
                                stream.close()

                                itemsCountDownLatch.countDown()
                                DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")

                                DragonflyLogger.error(LOG_TAG, "uploadTask - failure | error: ${exception.message}")
                            })
                            .addOnSuccessListener(executor, OnSuccessListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                                stream.close()

                                itemsCountDownLatch.countDown()
                                DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")

                                taskSnapshot.metadata?.let {
                                    val gcsPath = "${it.bucket}/${it.path}"
                                    val updatedFeedback = feedback.copy(
                                            imageGcsPath = gcsPath,
                                            uploadToGcsFinished = true
                                    )

                                    databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).child(feedback.key).setValue(updatedFeedback)
                                    pendingFeedbackRepository.delete(feedback.key!!)

                                    DragonflyLogger.debug(LOG_TAG, "uploadTask - success | feedback successfully updated with gcsPath: ${gcsPath}")
                                }
                            })
                } catch (e: Exception) {
                    stream.close()

                    itemsCountDownLatch.countDown()
                    DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
                }
            } catch (e: FileNotFoundException) {
                DragonflyLogger.warn(LOG_TAG, e.message!!)

                pendingFeedbackRepository.delete(feedback.key!!)

                itemsCountDownLatch.countDown()
                DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
            }
        }

        try {
            itemsCountDownLatch.await()
            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch wait finished")
        } catch (ignored: InterruptedException) {

        }
    }

    companion object {
        private val LOG_TAG = StashedFeedbackProcessor::class.java.simpleName
    }
}