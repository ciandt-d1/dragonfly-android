package com.ciandt.dragonfly.example.features.download

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.data.model.ModelManager
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.helpers.DownloadNotificationHelper
import com.ciandt.dragonfly.example.infrastructure.extensions.getDownloadManager
import com.ciandt.dragonfly.example.infrastructure.extensions.getLocalBroadcastManager
import java.io.File
import java.io.FileOutputStream

class DownloadIntentService : IntentService("DownloadIntentService") {

    private lateinit var downloadedFile: DownloadedFile

    override fun onHandleIntent(intent: Intent?) {

        if (intent == null) {
            return
        }

        downloadedFile = intent.getParcelableExtra<DownloadedFile>(DOWNLOAD_FILE_BUNDLE)

        if (downloadedFile.isSuccessful()) {
            handleSuccess()

        } else if (downloadedFile.isFailed()) {

            when (downloadedFile.reason) {
                401, 403 -> handleUnauth()
                404 -> handleNotFound()
                else -> handleError()
            }
        }

        val downloadManager = getDownloadManager()
        downloadManager.remove(downloadedFile.id)
    }

    private fun handleSuccess() {

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

        // MOCKED
        val model = ModelManager.loadModels().last()
        model.version = 2
        model.size = outputFile.length()
        model.status = Model.STATUS_DOWNLOADED
        sendBroadcast(model)
    }

    private fun handleError() {
        DownloadNotificationHelper.showError(this, downloadedFile)
    }

    private fun handleNotFound() {
        handleError()
    }

    private fun handleUnauth() {
        handleError()
    }

    private fun sendBroadcast(model: Model) {
        getLocalBroadcastManager().sendBroadcast(ModelChangedReceiver.create(model))
    }

    companion object {

        private val DOWNLOAD_FILE_BUNDLE = "${BuildConfig.APPLICATION_ID}.download_file_bundle"

        fun create(context: Context, file: DownloadedFile): Intent {
            val intent = Intent(context, DownloadIntentService::class.java)
            intent.putExtra(DOWNLOAD_FILE_BUNDLE, file)
            return intent
        }
    }
}
