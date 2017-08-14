package com.ciandt.dragonfly.example.infrastructure.jobs

import com.ciandt.dragonfly.example.features.feedback.jobs.ProcessStashedFeedbackJob
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * Created by iluz on 8/3/17.
 */
class DragonflyJobCreator : JobCreator {
    override fun create(jobTag: String): Job = when (jobTag) {
        ProcessStashedFeedbackJob.JOB_TAG -> ProcessStashedFeedbackJob()
        else -> throw IllegalArgumentException("Unknown job tag specified.")
    }
}