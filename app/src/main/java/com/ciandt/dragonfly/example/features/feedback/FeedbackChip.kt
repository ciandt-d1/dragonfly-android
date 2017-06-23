package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.tensorflow.Classifier

data class FeedbackChip(val recognition: Classifier.Recognition) : Chip {

    override fun getText(): String = "%s %.0f%%".format(recognition.title, recognition.confidence * 100)

    override fun toString(): String = "FeedbackChip(recognition='$recognition')"

}