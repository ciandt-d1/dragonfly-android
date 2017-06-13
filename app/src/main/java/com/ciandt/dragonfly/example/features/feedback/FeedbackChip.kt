package com.ciandt.dragonfly.example.features.feedback

import com.ciandt.dragonfly.example.components.chips.Chip
import com.ciandt.dragonfly.tensorflow.Classifier

data class FeedbackChip(val recognition: Classifier.Recognition, private var selected: Boolean = false) : Chip {

    override fun getText(): String = "%s %.0f%%".format(recognition.title, recognition.confidence * 100)

    override fun toString(): String = "FeedbackChip(recognition='$recognition', selected=$selected)"

    override fun isSelected(): Boolean = selected

    override fun setSelected(status: Boolean) {
        this.selected = status
    }
}