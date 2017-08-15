package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Created by iluz on 8/15/17.
 */
class PendingFeedbackEntityToFeedbackMapperTest {
    @Test
    fun shouldMapFromPendingFeedbackEntityToFeedback() {
        val feedbackId = "1234"

        val pendingFeedback = PendingFeedbackEntitiy(
                id = feedbackId,
                tenant = "ciandt",
                project = "flowers",
                userId = "567",
                modelVersion = 1,
                value = 1,
                actualLabel = "keyboard",
                identifiedLabels = listOf(
                        PendingFeedbackLabelEntitiy(
                                id = 1,
                                feedbackId = feedbackId,
                                label = "keyboard",
                                confidence = 0.5f
                        ),
                        PendingFeedbackLabelEntitiy(
                                id = 2,
                                feedbackId = feedbackId,
                                label = "mouse",
                                confidence = 0.12f
                        )
                ),
                imageLocalPath = "/local/path/to/image",
                createdAt = System.currentTimeMillis()
        )

        val feedback = PendingFeedbackEntityToFeedbackMapper(pendingFeedback).map()
        feedback!!.let { feedback ->
            assertEquals(pendingFeedback.id, feedback.key)
            assertEquals(pendingFeedback.tenant, feedback.tenant)
            assertEquals(pendingFeedback.project, feedback.project)
            assertEquals(pendingFeedback.userId, feedback.userId)
            assertEquals(pendingFeedback.modelVersion, feedback.modelVersion)
            assertEquals(pendingFeedback.value, feedback.value)
            assertEquals(pendingFeedback.actualLabel, feedback.actualLabel)
            assertEquals(pendingFeedback.imageLocalPath, feedback.imageLocalPath)
            assertNull(feedback.imageGcsPath)
            assertFalse(feedback.uploadToGcsFinished)
            assertEquals(pendingFeedback.createdAt, feedback.createdAt)
            assertNull(feedback.tenantUserProject)

            assertEquals(feedback.identifiedLabels.keys.size, pendingFeedback.identifiedLabels.size)
            pendingFeedback.identifiedLabels.forEach { label ->
                assertNotNull(feedback.identifiedLabels.get(label.label))
                assertEquals(feedback.identifiedLabels.get(label.label), label.confidence)
            }
        }
    }
}