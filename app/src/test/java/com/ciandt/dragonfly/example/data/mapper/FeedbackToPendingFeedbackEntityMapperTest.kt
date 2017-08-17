package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

/**
 * Created by iluz on 8/15/17.
 */
class FeedbackToPendingFeedbackEntityMapperTest {
    @Test
    fun shouldMapFromFeedbackToPendingFeedbackEntity() {
        val feedback = Feedback(
                key = "1234",
                tenant = "ciandt",
                project = "flowers",
                userId = "567",
                modelVersion = 1,
                value = 1,
                actualLabel = "keyboard",
                identifiedLabels = mapOf(
                        "keyboard" to 0.5f,
                        "mouse" to 0.12f,
                        "monitor" to 0.03f
                ),
                imageLocalPath = "/local/path/to/image",
                imageGcsPath = "gs://dragonfly/ciandt/567/1234abc.jpg",
                uploadToGcsFinished = true,
                createdAt = System.currentTimeMillis(),
                tenantUserProject = "ciandt_567_flowers"
        )

        val pendingFeedback = FeedbackToPendingFeedbackEntityMapper(feedback).map()
        pendingFeedback.let {
            feedback.key!!.shouldEqualTo(it.id)
            feedback.tenant.shouldEqualTo(it.tenant)
            feedback.project.shouldEqualTo(it.project)
            feedback.userId.shouldEqualTo(it.userId)
            feedback.modelVersion.shouldEqualTo(it.modelVersion)
            feedback.value.shouldEqualTo(it.value)
            feedback.actualLabel.shouldEqualTo(it.actualLabel)
            feedback.imageLocalPath.shouldEqualTo(it.imageLocalPath)
            feedback.createdAt.shouldEqualTo(it.createdAt)

            feedback.identifiedLabels.size.shouldEqualTo(it.identifiedLabels.size)
            it.identifiedLabels.forEach { label ->
                feedback.identifiedLabels.get(label.label).shouldNotBeNull()
                feedback.identifiedLabels.get(label.label)!!.shouldEqualTo(label.confidence)
            }
        }
    }
}