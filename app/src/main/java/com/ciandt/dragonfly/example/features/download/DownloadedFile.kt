package com.ciandt.dragonfly.example.features.download

import android.app.DownloadManager
import android.os.Parcel
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator

data class DownloadedFile(
        val id: Long,
        val title: String,
        val status: Int,
        val reason: Int,
        val bytesTotal: Int = 0,
        val bytesDownloaded: Int = 0,
        val lastModifiedAt: Int = 0) : KParcelable {

    val statusText: String
        get() {
            when (status) {
                DownloadManager.STATUS_FAILED -> return "STATUS_FAILED"
                DownloadManager.STATUS_PAUSED -> return "STATUS_PAUSED"
                DownloadManager.STATUS_PENDING -> return "STATUS_PENDING"
                DownloadManager.STATUS_RUNNING -> return "STATUS_RUNNING"
                DownloadManager.STATUS_SUCCESSFUL -> return "STATUS_SUCCESSFUL"
                STATUS_CANCELLED -> return "STATUS_CANCELLED"
                else -> return ""
            }
        }

    val reasonText: String
        get() {
            when (reason) {
                DownloadManager.ERROR_CANNOT_RESUME -> return "ERROR_CANNOT_RESUME"
                DownloadManager.ERROR_DEVICE_NOT_FOUND -> return "ERROR_DEVICE_NOT_FOUND"
                DownloadManager.ERROR_FILE_ALREADY_EXISTS -> return "ERROR_FILE_ALREADY_EXISTS"
                DownloadManager.ERROR_FILE_ERROR -> return "ERROR_FILE_ERROR"
                DownloadManager.ERROR_HTTP_DATA_ERROR -> return "ERROR_HTTP_DATA_ERROR"
                DownloadManager.ERROR_INSUFFICIENT_SPACE -> return "ERROR_INSUFFICIENT_SPACE"
                DownloadManager.ERROR_TOO_MANY_REDIRECTS -> return "ERROR_TOO_MANY_REDIRECTS"
                DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> return "ERROR_UNHANDLED_HTTP_CODE"
                DownloadManager.ERROR_UNKNOWN -> return "ERROR_UNKNOWN"
                DownloadManager.PAUSED_QUEUED_FOR_WIFI -> return "PAUSED_QUEUED_FOR_WIFI"
                DownloadManager.PAUSED_UNKNOWN -> return "PAUSED_UNKNOWN"
                DownloadManager.PAUSED_WAITING_FOR_NETWORK -> return "PAUSED_WAITING_FOR_NETWORK"
                DownloadManager.PAUSED_WAITING_TO_RETRY -> return "PAUSED_WAITING_TO_RETRY"
                else -> return ""
            }
        }

    fun isPending(): Boolean = status == DownloadManager.STATUS_PENDING

    fun isRunning(): Boolean = status == DownloadManager.STATUS_RUNNING

    fun isPaused(): Boolean = status == DownloadManager.STATUS_PAUSED

    fun isSuccessful(): Boolean = status == DownloadManager.STATUS_SUCCESSFUL

    fun isFailed(): Boolean = status == DownloadManager.STATUS_FAILED

    fun isCancelled(): Boolean = status == STATUS_CANCELLED

    fun isFinished(): Boolean = isSuccessful() || isFailed() || isCancelled()

    override fun toString(): String {
        return "DownloadedFile(" + "\n" +
                "\tid=$id, " + "\n" +
                "\ttitle=$title, " + "\n" +
                "\tstatus=$status, " + "\n" +
                "\tstatusText=$statusText, " + "\n" +
                "\treason=$reason, " + "\n" +
                "\treasonText=$reasonText, " + "\n" +
                "\tbytesTotal=$bytesTotal, " + "\n" +
                "\tbytesDownloaded=$bytesDownloaded, " + "\n" +
                "\tlastModifiedAt=$lastModifiedAt" + "\n" +
                ")"
    }

    private constructor(p: Parcel) : this(
            p.readLong(),
            p.readString(),
            p.readInt(),
            p.readInt(),
            p.readInt(),
            p.readInt(),
            p.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(title)
        writeInt(status)
        writeInt(reason)
        writeInt(bytesTotal)
        writeInt(bytesDownloaded)
        writeInt(lastModifiedAt)
    }

    companion object {

        @JvmField val CREATOR = parcelableCreator(::DownloadedFile)

        private val STATUS_CANCELLED = -1

        fun cancelled(id: Long): DownloadedFile {
            return DownloadedFile(id, "", STATUS_CANCELLED, 0)
        }
    }
}