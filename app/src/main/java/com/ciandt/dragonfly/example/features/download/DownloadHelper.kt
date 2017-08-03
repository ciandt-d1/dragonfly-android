package com.ciandt.dragonfly.example.features.download

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.data.ProjectRepository
import com.ciandt.dragonfly.example.helpers.ZipHelper
import com.ciandt.dragonfly.example.infrastructure.extensions.getDownloadManager
import com.ciandt.dragonfly.example.models.Version
import java.io.File

object DownloadHelper {

    fun startDownload(context: Context, title: String, version: Version, uri: Uri) = runOnBackgroundThread {
        val request = DownloadManager.Request(uri)

        val appName = context.getString(R.string.app_name)
        request.setTitle("$appName: $title")

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setVisibleInDownloadsUi(false)

        val id = context.getDownloadManager().enqueue(request)

        with(ProjectRepository(context)) {
            updateVersionStatus(version.project, version.version, Version.STATUS_DOWNLOADING)
            saveDownload(id, version.project, version.version)
        }
    }

    fun startDownloadHandler(context: Context, id: Long) = runOnBackgroundThread {
        val repository = ProjectRepository(context)

        val version = repository.getVersionByDownload(id) ?: return@runOnBackgroundThread
        val file = getDownloadedFile(context, id)

        repository.deleteDownload(id)
        context.startService(DownloadHandlerService.create(context, file, version))
    }

    fun processDownload(context: Context, downloadedFile: DownloadedFile, version: Version, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        val uri = context.getDownloadManager().getUriForDownloadedFile(downloadedFile.id)
        val inputStream = context.contentResolver.openInputStream(uri)

        val path = arrayListOf<String>(
                context.filesDir.absolutePath,
                version.project,
                version.version.toString()
        ).joinToString(File.separator)

        ZipHelper.unzip(inputStream, path,
                onSuccess = { files ->

                    val labelsPath = files.filter { it.contains(".txt", true) }.firstOrNull()
                    val modelPath = files.filter { it.contains(".pb", true) }.firstOrNull()

                    if (labelsPath == null || modelPath == null) {
                        onFailure(RuntimeException("Downloaded version does not contain .txt file or .pb file"))
                        return@unzip
                    }

                    version.labelsPath = labelsPath
                    version.modelPath = modelPath
                    version.status = Version.STATUS_DOWNLOADED

                    updateVersion(context, version)
                    onSuccess()
                },
                onFailure = onFailure
        )
    }

    private fun updateVersion(context: Context, version: Version) = runOnBackgroundThread {
        ProjectRepository(context).updateVersion(version)
    }

    private fun getDownloadedFile(context: Context, id: Long): DownloadedFile {
        val query = DownloadManager.Query()
        query.setFilterById(id)

        val cursor = context.getDownloadManager().query(query)
        if (cursor.moveToFirst()) {
            return cursorToDownloadedFile(cursor)
        } else {
            return DownloadedFile.cancelled(id)
        }
    }

    private fun cursorToDownloadedFile(cursor: Cursor): DownloadedFile {
        val id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID))
        val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        val reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON))
        val totalSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val downloadedSize = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val lastModifiedAt = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP))
        return DownloadedFile(id, title, status, reason, totalSize, downloadedSize, lastModifiedAt)
    }

    private fun runOnBackgroundThread(action: () -> Unit) {
        Thread(Runnable { action() }).start()
    }
}