package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.tensorflow.Classifier

data class FeedbackChip(val classification: Classifier.Classification) : Chip {

    override fun getText(): String = "%s %.0f%%".format(classification.title, classification.confidence * 100)

    override fun toString(): String = "FeedbackChip(classification='$classification')"

}