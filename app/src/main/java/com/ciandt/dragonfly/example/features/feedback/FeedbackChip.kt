package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.tensorflow.Classifier

data class FeedbackChip(val classification: Classifier.Classification) : Chip {

    override fun getText(): String {
        return if (!isOther()) {
            "%s %.0f%%".format(classification.title, classification.confidence * 100)
        } else {
            "%s".format(classification.title)
        }
    }

    fun isOther(): Boolean = classification.id == OTHER_ID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as FeedbackChip
        return classification.id == other.classification.id
    }

    override fun hashCode(): Int {
        return classification.id.hashCode()
    }

    override fun toString(): String = "FeedbackChip(classification='$classification')"

    companion object {
        private val OTHER_ID = "other"

        fun createOther(title: String): FeedbackChip {
            return FeedbackChip(createOtherClassification(title))
        }

        fun createOtherClassification(title: String): Classifier.Classification {
            return Classifier.Classification(OTHER_ID, title, 0.0f, null)
        }
    }
}