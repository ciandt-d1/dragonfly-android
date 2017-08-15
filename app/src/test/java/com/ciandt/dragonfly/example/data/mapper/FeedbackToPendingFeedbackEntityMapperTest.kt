package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.features.feedback.model.Feedback
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
            assertEquals(feedback.key, it.id)
            assertEquals(feedback.tenant, it.tenant)
            assertEquals(feedback.project, it.project)
            assertEquals(feedback.userId, it.userId)
            assertEquals(feedback.modelVersion, it.modelVersion)
            assertEquals(feedback.value, it.value)
            assertEquals(feedback.actualLabel, it.actualLabel)
            assertEquals(feedback.imageLocalPath, it.imageLocalPath)
            assertEquals(feedback.createdAt, it.createdAt)

            assertEquals(feedback.identifiedLabels.keys.size, it.identifiedLabels.size)
            it.identifiedLabels.forEach { label ->
                assertNotNull(feedback.identifiedLabels.get(label.label))
                assertEquals(feedback.identifiedLabels.get(label.label), label.confidence)
            }
        }
    }
}