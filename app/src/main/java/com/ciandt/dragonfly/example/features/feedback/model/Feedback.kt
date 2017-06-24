package com.ciandt.dragonfly.example.features.feedback.model

/**
 * Created by iluz on 6/23/17.
 */

data class Feedback(
        val tenant: String,
        val project: String,
        val userId: String,
        val modelVersion: Int,
        val value: Int,
        val actualLabel: String,
        val identifiedLabels: Map<String, Float>,
        val imageLocalPath: String,
        val imageGcsPath: String? = null,
        val uploadToGcsFinished: Boolean = false,
        val createdAt: Long = System.currentTimeMillis()
) {

    override fun toString(): String {
        return "Feedback(tenant='$tenant', project='$project', userId='$userId', modelVersion=$modelVersion, value=$value, actualLabel='$actualLabel', identifiedLabels=$identifiedLabels, imageLocalPath='$imageLocalPath', imageGcsPath=$imageGcsPath, uploadToGcsFinished=$uploadToGcsFinished, createdAt=$createdAt)"
    }

    companion object {
        val POSITIVE = 1
        val NEGATIVE = 0
    }
}