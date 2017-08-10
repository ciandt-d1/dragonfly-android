package com.ciandt.dragonfly.example.infrastructure.jobs.processor

import android.os.Looper
import android.os.NetworkOnMainThreadException
import android.support.annotation.WorkerThread
import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.lastSegment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
class StashedFeedbackProcessor(database: FirebaseDatabase, storage: FirebaseStorage, val tenant: String, val userId: String) : Processor {
    private val stashDbRef = database.reference.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH)
    private val databaseRef = database.reference
    private val storageRef = storage.reference

    private var mainCountDownLatch = CountDownLatch(1)
    private lateinit var itemsCountDownLatch: CountDownLatch

//    init {
//        feedbackSaverInteractor.setOnFeedbackSaveErrorCallback { feedback, exception ->
//            DragonflyLogger.debug(LOG_TAG, "Failed to save feedback $feedback with error '${exception.message}'")
//
//            itemsCountDownLatch.countDown()
//            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
//        }
//
//        feedbackSaverInteractor.setOnFeedbackUpdatedWithGcsLocationCallback { feedback ->
//            DragonflyLogger.debug(LOG_TAG, "Feedback updated with GCS: $feedback")
//
//            itemsCountDownLatch.countDown()
//            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
//        }
//    }

    @WorkerThread
    override fun process() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw NetworkOnMainThreadException()
        }

        val startAt = "${tenant}__${userId}__"
        val endAt = "$startAt\uf8ff"

        DragonflyLogger.debug(LOG_TAG, "main thread: ${Looper.getMainLooper()}")


        DragonflyLogger.debug(LOG_TAG, "current thread: ${Looper.myLooper()}")

        val executor = Executors.newSingleThreadExecutor();

        stashDbRef
                .orderByChild(QUERY_FIELD)
                .startAt(startAt)
                .endAt(endAt)
                .limitToFirst(FirebaseConfig.SYNC_ITEMS_PER_RUN)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError?) {
                        DragonflyLogger.error(LOG_TAG, databaseError.toString())
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        DragonflyLogger.debug(LOG_TAG, "onDataChange() - current thread: ${Looper.myLooper()}")

                        var childCount: Int = dataSnapshot?.children?.count() ?: 0
                        if (childCount == 0) {
                            mainCountDownLatch.countDown()

                            return
                        }

                        itemsCountDownLatch = CountDownLatch(childCount)

                        dataSnapshot!!.children.forEach { child ->
                            val feedback = child.getValue(Feedback::class.java) ?: return

                            if (feedback.uploadToGcsFinished) {
                                return
                            }

                            DragonflyLogger.debug(LOG_TAG, "dataSnapshot.forEach() - current thread: ${Looper.myLooper()}")

                            DragonflyLogger.debug(LOG_TAG, "Saving feedback: $feedback")

//                            feedback.key = child.key
//                            feedbackSaverInteractor.saveFeedback(feedback)

                            try {
                                val stream = FileInputStream(feedback.imageLocalPath)
                                try {
                                    val fileName = feedback.imageLocalPath.lastSegment(File.separator)
                                    val imageReference = storageRef.child("${feedback.tenant}/${feedback.userId}/${fileName}")
                                    val metadata = StorageMetadata.Builder()
                                            .setContentType("image/jpeg")
                                            .build()

                                    imageReference.putStream(stream, metadata)
                                            .addOnFailureListener(executor, object : OnFailureListener {
                                                override fun onFailure(exception: Exception) {
                                                    DragonflyLogger.debug(LOG_TAG, "onFailure() - current thread: ${Looper.myLooper()}")

                                                    itemsCountDownLatch.countDown()
                                                    DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")

                                                    stream.close()

                                                    DragonflyLogger.error(LOG_TAG, "uploadTask - failure | error: ${exception.message}")
                                                }

                                            })
                                            .addOnSuccessListener(executor, object : OnSuccessListener<UploadTask.TaskSnapshot> {
                                                override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                                                    stream.close()

                                                    taskSnapshot.metadata?.let {
                                                        itemsCountDownLatch.countDown()
                                                        DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")

                                                        val gcsPath = "${it.bucket}/${it.path}"
                                                        val updatedFeedback = feedback.copy(
                                                                imageGcsPath = gcsPath,
                                                                uploadToGcsFinished = true
                                                        )

                                                        databaseRef.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH).child(child.key).setValue(updatedFeedback)

                                                        DragonflyLogger.debug(LOG_TAG, "uploadTask - success | feedback successfully updated with gcsPath: ${gcsPath}")
                                                    }
                                                }
                                            })
                                } catch (e: Exception) {
                                    DragonflyLogger.debug(LOG_TAG, "catch Exception() - current thread: ${Looper.myLooper()}")

                                    stream.close()

                                    itemsCountDownLatch.countDown()
                                    DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
                                }
                            } catch (e: FileNotFoundException) {
                                DragonflyLogger.debug(LOG_TAG, "catch FileNotFoundException() - current thread: ${Looper.myLooper()}")

                                DragonflyLogger.warn(LOG_TAG, e.message!!)

                                itemsCountDownLatch.countDown()
                                DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch decreased to ${itemsCountDownLatch.count}")
                            }
                        }

                        try {
                            itemsCountDownLatch.await()
                            DragonflyLogger.debug(LOG_TAG, "itemsCountDownLatch wait finished")

                            mainCountDownLatch.countDown()
                        } catch (ignored: InterruptedException) {

                        }
                    }
                })

        stashDbRef.keepSynced(true)

        try {
            mainCountDownLatch.await()
            DragonflyLogger.debug(LOG_TAG, "mainCountDownLatch wait finished")
        } catch (ignored: InterruptedException) {

        }
    }

    companion object {
        private val LOG_TAG = StashedFeedbackProcessor::class.java.simpleName

        private val QUERY_FIELD = "tenantUserProject"
    }

}