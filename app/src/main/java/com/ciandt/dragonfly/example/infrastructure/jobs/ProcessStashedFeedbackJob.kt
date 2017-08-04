package com.ciandt.dragonfly.example.infrastructure.jobs

import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.config.Tenant
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.jobs.processor.StashedFeedbackProcessor
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit


/**
 * Created by iluz on 8/3/17.
 */
class ProcessStashedFeedbackJob : Job() {
    override fun onRunJob(params: Params?): Result {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            DragonflyLogger.warn(LOG_TAG, "Tried to execute ProcessStashedFeedbackJob, but there is no user signed in.")
            return Result.FAILURE
        }

        try {
            val processor = StashedFeedbackProcessor(FirebaseDatabase.getInstance(), Tenant.ID, currentUser.uid)
            processor.process()

            return Result.SUCCESS
        } catch (e: Exception) {
            DragonflyLogger.error(LOG_TAG, e)
            return Result.FAILURE
        }
    }

    companion object {
        val JOB_TAG = ProcessStashedFeedbackJob::class.java.simpleName

        private val LOG_TAG = ProcessStashedFeedbackJob::class.java.simpleName

        private val JOB_INTERVAL = if (BuildConfig.DEBUG) TimeUnit.MINUTES.toMillis(1) else TimeUnit.MINUTES.toMillis(60)
        private val JOB_FLEX = if (BuildConfig.DEBUG) TimeUnit.SECONDS.toMillis(30) else TimeUnit.MINUTES.toMillis(10)

        fun schedule() {
            JobRequest.Builder(JOB_TAG)
                    .setPeriodic(JOB_INTERVAL, JOB_FLEX)
                    .setRequiresCharging(true)
                    .setRequiresDeviceIdle(false)
                    .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                    .setRequirementsEnforced(true)
                    .setUpdateCurrent(true)
                    .setPersisted(true)
                    .build()
                    .schedule()
        }
    }
}