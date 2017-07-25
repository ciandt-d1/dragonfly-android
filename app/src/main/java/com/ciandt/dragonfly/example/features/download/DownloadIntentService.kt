package com.ciandt.dragonfly.example.features.download

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.helpers.DownloadNotificationHelper
import com.ciandt.dragonfly.example.infrastructure.extensions.getDownloadManager
import java.io.File
import java.io.FileOutputStream

class DownloadIntentService : IntentService("DownloadIntentService") {

    override fun onHandleIntent(intent: Intent?) {

        if (intent == null) {
            return
        }

        val downloadedFile = intent.getParcelableExtra<DownloadedFile>(DOWNLOAD_BUNDLE)

        if (downloadedFile.isSuccessful()) {
            handleSuccess(downloadedFile)

        } else if (downloadedFile.isFailed()) {

            when (downloadedFile.reason) {
                401, 403 -> handleUnauth(downloadedFile)
                404 -> handleNotFound(downloadedFile)
                else -> handleError(downloadedFile)
            }

        }

        val downloadManager = getDownloadManager()
        downloadManager.remove(downloadedFile.id)
    }

    private fun handleSuccess(downloadedFile: DownloadedFile) {

        DownloadNotificationHelper.showProcessing(this, downloadedFile)

        val downloadManager = getDownloadManager()
        val uri = downloadManager.getUriForDownloadedFile(downloadedFile.id)

        val inputStream = contentResolver.openInputStream(uri)

        val outputFile = File(filesDir, downloadedFile.id.toString())
        val outputStream = FileOutputStream(outputFile)

        val buf = ByteArray(1024)
        var len: Int = 0

        while ({ len = inputStream.read(buf); len }() > 0) {
            outputStream.write(buf, 0, len)
        }

        outputStream.close()
        inputStream.close()

        DownloadNotificationHelper.showFinished(this, downloadedFile)
    }

    private fun handleError(file: DownloadedFile) {
        DownloadNotificationHelper.showError(this, file)
    }

    private fun handleNotFound(file: DownloadedFile) {
        handleError(file)
    }

    private fun handleUnauth(file: DownloadedFile) {
        handleError(file)
    }

    companion object {

        private val DOWNLOAD_BUNDLE = "${BuildConfig.APPLICATION_ID}.download_bundle"

        fun create(context: Context, file: DownloadedFile): Intent {
            val intent = Intent(context, DownloadIntentService::class.java)
            intent.putExtra(DOWNLOAD_BUNDLE, file)
            return intent
        }
    }
}
