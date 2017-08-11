package com.ciandt.dragonfly.example.data.local.relations

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackEntitiy
import com.ciandt.dragonfly.example.data.local.entities.PendingFeedbackLabelEntitiy
import com.ciandt.dragonfly.example.data.mapper.Mapper

class PendingFeedbackWithLabels : Mapper<PendingFeedbackEntitiy>() {

    @Embedded
    var feedback: PendingFeedbackEntitiy? = null

    @Relation(parentColumn = "id", entityColumn = "feedback_id", entity = PendingFeedbackLabelEntitiy::class)
    var labels: List<PendingFeedbackLabelEntitiy> = emptyList()

    override fun map(): PendingFeedbackEntitiy? {

        feedback?.let {
            it.identifiedLabels = labels
            return it
        }

        return feedback
    }
}