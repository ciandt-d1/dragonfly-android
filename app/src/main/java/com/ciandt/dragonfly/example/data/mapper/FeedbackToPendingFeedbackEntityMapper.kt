package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import com.ciandt.dragonfly.example.features.feedback.model.Feedback

class FeedbackToPendingFeedbackEntityMapper(val feedback: Feedback) : Mapper<PendingFeedbackEntitiy>() {

    override fun map(): PendingFeedbackEntitiy = with(feedback) {
        val identifiedLabels: List<PendingFeedbackLabelEntitiy> = feedback.identifiedLabels.map {
            val (label, confidence) = it

            PendingFeedbackLabelEntitiy(
                    feedbackId = feedback.key!!,
                    label = label,
                    confidence = confidence)
        }

        return PendingFeedbackEntitiy(
                id = feedback.key!!,
                tenant = feedback.tenant,
                project = feedback.project,
                userId = feedback.userId,
                modelVersion = feedback.modelVersion,
                value = feedback.value,
                actualLabel = feedback.actualLabel,
                identifiedLabels = identifiedLabels,
                imageLocalPath = feedback.imageLocalPath,
                createdAt = feedback.createdAt
        )
    }
}