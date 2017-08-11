package com.ciandt.dragonfly.example.data.mapper

import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.features.feedback.model.Feedback

class PendingFeedbackEntityToFeedbackMapper(val entity: PendingFeedbackEntitiy) : Mapper<Feedback>() {

    override fun map(): Feedback? = with(entity) {

        val scoreByLabelMap = identifiedLabels.associateBy(
                keySelector = { label -> label.label },
                valueTransform = { label -> label.confidence }
        )

        return Feedback(
                key = entity.id,
                tenant = entity.tenant,
                project = entity.project,
                userId = entity.userId,
                modelVersion = entity.modelVersion,
                value = entity.modelVersion,
                actualLabel = entity.actualLabel,
                identifiedLabels = scoreByLabelMap,
                imageLocalPath = entity.imageLocalPath,
                createdAt = entity.createdAt
        )
    }
}