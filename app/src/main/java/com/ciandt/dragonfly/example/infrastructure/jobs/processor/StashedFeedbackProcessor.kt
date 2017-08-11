package com.ciandt.dragonfly.example.infrastructure.jobs.processor

import com.ciandt.dragonfly.example.config.FirebaseConfig
import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Created by iluz on 8/4/17.
 */
class StashedFeedbackProcessor(database: FirebaseDatabase, val tenant: String, val userId: String) : Processor {
    private val stashDbRef = database.reference.child(FirebaseConfig.COLLECTION_FEEDBACK_STASH)

    override fun process() {
        val startAt = "${tenant}__${userId}__"
        val endAt = "${startAt}\uf8ff"

        stashDbRef.orderByChild(QUERY_FIELD).startAt(startAt).endAt(endAt).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError?) {
                DragonflyLogger.error(LOG_TAG, databaseError.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    it.children.forEach { child ->
                        val feedback = child.getValue(Feedback::class.java)
                        DragonflyLogger.debug(LOG_TAG, feedback.toString())
                    }
                }
            }

        })
    }

    companion object {
        private val LOG_TAG = StashedFeedbackProcessor::class.java.simpleName

        private val QUERY_FIELD = "tenantUserProject"
    }
}