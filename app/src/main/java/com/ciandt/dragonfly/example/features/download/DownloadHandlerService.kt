package com.ciandt.dragonfly.example.features.download

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.data.ProjectRepository
import com.ciandt.dragonfly.example.helpers.DownloadNotificationHelper
import com.ciandt.dragonfly.example.infrastructure.DragonflyLogger
import com.ciandt.dragonfly.example.infrastructure.extensions.getDownloadManager
import com.ciandt.dragonfly.example.infrastructure.extensions.getLocalBroadcastManager
import com.ciandt.dragonfly.example.models.Version

class DownloadHandlerService : IntentService("DownloadHandlerService") {

    private lateinit var downloadedFile: DownloadedFile
    private lateinit var version: Version

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        downloadedFile = intent.getParcelableExtra<DownloadedFile>(DOWNLOAD_FILE_BUNDLE)
        version = intent.getParcelableExtra<Version>(VERSION_BUNDLE)

        if (downloadedFile.isSuccessful()) {
            handleDownloadSuccess()

        } else if (downloadedFile.isFailed()) {

            when (downloadedFile.reason) {
                401, 403 -> handleUnauth()
                404 -> handleNotFound()
                else -> handleError()
            }
        }

        getDownloadManager().remove(downloadedFile.id)
    }

    private fun handleDownloadSuccess() {
        DownloadNotificationHelper.showProcessing(this, downloadedFile)
        DownloadHelper.processDownload(this, downloadedFile, version, onSuccess = { newVersion ->

            DownloadNotificationHelper.showFinished(this, downloadedFile, newVersion.toLibraryModel())
            sendBroadcastForProjectChanged()

        }, onFailure = { exception ->

            DragonflyLogger.exception(DownloadHandlerService.LOG_TAG, exception)
            handleError()
        })
    }

    private fun handleError() {
        DownloadNotificationHelper.showError(this, downloadedFile)
        ProjectRepository(this).updateVersionStatus(version.project, version.version, Version.STATUS_NOT_DOWNLOADED)
        sendBroadcastForProjectChanged()
    }

    private fun handleNotFound() {
        handleError()
    }

    private fun handleUnauth() {
        handleError()
    }

    private fun sendBroadcastForProjectChanged() {
        ProjectRepository(this).getProject(version.project)?.let {
            getLocalBroadcastManager().sendBroadcast(ProjectChangedReceiver.create(it))
        }
    }

    companion object {

        private val LOG_TAG = DownloadHandlerService::class.java.simpleName

        private val DOWNLOAD_FILE_BUNDLE = "${BuildConfig.APPLICATION_ID}.download_file_bundle"
        private val VERSION_BUNDLE = "${BuildConfig.APPLICATION_ID}.version_bundle"

        fun create(context: Context, file: DownloadedFile, version: Version): Intent {
            val intent = Intent(context, DownloadHandlerService::class.java)
            intent.putExtra(DOWNLOAD_FILE_BUNDLE, file)
            intent.putExtra(VERSION_BUNDLE, version)
            return intent
        }
    }
}
