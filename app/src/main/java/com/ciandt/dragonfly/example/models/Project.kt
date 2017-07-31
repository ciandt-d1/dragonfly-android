package com.ciandt.dragonfly.example.models

import android.os.Parcel
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator

data class Project(
        var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: List<String> = emptyList(),
        var versions: List<Version> = emptyList()
) : KParcelable {

    fun hasUpdate(): Boolean {
        return false
    }

    var status: Int = 0

    fun isDownloading(): Boolean {
        return status == STATUS_DOWNLOADING
    }

    fun isDownloaded(): Boolean {
        return status == STATUS_DOWNLOADED
    }

    private constructor(p: Parcel) : this(
            p.readString(),
            p.readString(),
            p.readString(),
            p.createStringArrayList(),
            ArrayList<Version>().apply { p.readList(this, Version::class.java.classLoader) }
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeStringList(colors)
        dest.writeList(versions)
    }

    companion object {

        val STATUS_DEFAULT = 0
        val STATUS_DOWNLOADING = 1
        val STATUS_DOWNLOADED = 2

        @JvmField val CREATOR = parcelableCreator(::Project)
    }
}