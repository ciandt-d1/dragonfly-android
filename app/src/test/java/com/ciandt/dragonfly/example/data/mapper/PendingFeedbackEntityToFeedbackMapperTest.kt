package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

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

        val feedback = PendingFeedbackEntityToFeedbackMapper(pendingFeedback).map()!!

        pendingFeedback.id.shouldEqualTo(feedback.key!!)
        pendingFeedback.tenant.shouldEqualTo(feedback.tenant)
        pendingFeedback.project.shouldEqualTo(feedback.project)
        pendingFeedback.userId.shouldEqualTo(feedback.userId)
        pendingFeedback.modelVersion.shouldEqualTo(feedback.modelVersion)
        pendingFeedback.value.shouldEqualTo(feedback.value)
        pendingFeedback.actualLabel.shouldEqualTo(feedback.actualLabel)
        pendingFeedback.imageLocalPath.shouldEqualTo(feedback.imageLocalPath)
        feedback.imageGcsPath.shouldBeNull()
        feedback.uploadToGcsFinished.shouldBeFalse()
        pendingFeedback.createdAt.shouldEqualTo(feedback.createdAt)
        feedback.tenantUserProject.shouldBeNull()

        feedback.identifiedLabels.size.shouldEqualTo(pendingFeedback.identifiedLabels.size)
        pendingFeedback.identifiedLabels.forEach { label ->
            feedback.identifiedLabels.get(label.label).shouldNotBeNull()
            feedback.identifiedLabels.get(label.label)!!.shouldEqualTo(label.confidence)
        }
    }
}