package com.ciandt.dragonfly.example.features.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
        if (!DownloadHelper.isValid(context, id)) {
            return
        }

        val file = DownloadHelper.getDownloadedFile(context, id)

        DownloadHelper.removeDownload(context, id)

        if (file.isFinished()) {
            context.startService(DownloadIntentService.create(context, file))
        }
    }
}
