package com.ciandt.dragonfly.example.features.download

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.R
import com.ciandt.dragonfly.example.infrastructure.SharedPreferencesRepository
import com.ciandt.dragonfly.example.infrastructure.extensions.getDownloadManager

object DownloadHelper {

    private val PREFERENCES_PREFIX = "${BuildConfig.APPLICATION_ID}.preferences"
    private val DOWNLOADS_KEY = "$PREFERENCES_PREFIX.downloads"

    fun download(context: Context, title: String, uri: Uri) {

        val request = DownloadManager.Request(uri)

        val appName = context.getString(R.string.app_name)
        request.setTitle("$appName: $title")

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setVisibleInDownloadsUi(false)

        val id = context.getDownloadManager().enqueue(request)

        saveDownload(context, id)
    }

    fun getDownloadedFile(context: Context, id: Long): DownloadedFile {

        val downloadManager = context.getDownloadManager()

        val query = DownloadManager.Query()
        query.setFilterById(id)

        val cursor = downloadManager.query(query)
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

    fun isValid(context: Context, id: Long): Boolean {
        val preferencesRepository = SharedPreferencesRepository.get(context)
        val downloads = preferencesRepository.getStringSet(DOWNLOADS_KEY, emptySet())

        return id.toString() in downloads
    }

    fun removeDownload(context: Context, id: Long) {
        val preferencesRepository = SharedPreferencesRepository.get(context)

        val downloads = preferencesRepository.getStringSet(DOWNLOADS_KEY, HashSet<String>()) as MutableSet<String>
        downloads.remove(id.toString())

        preferencesRepository.putStringSet(DOWNLOADS_KEY, downloads)
    }

    fun saveDownload(context: Context, id: Long) {
        val preferencesRepository = SharedPreferencesRepository.get(context)

        val downloads = preferencesRepository.getStringSet(DOWNLOADS_KEY, HashSet<String>()) as MutableSet<String>
        downloads.add(id.toString())

        preferencesRepository.putStringSet(DOWNLOADS_KEY, downloads)
    }

}