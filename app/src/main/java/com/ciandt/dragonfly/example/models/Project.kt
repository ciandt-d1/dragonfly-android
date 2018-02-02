package com.ciandt.dragonfly.example.models

import android.os.Parcel
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator
import com.ciandt.dragonfly.example.shared.readBoolean
import com.ciandt.dragonfly.example.shared.writeBoolean

data class Project(
        var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: List<String> = emptyList(),
        var versions: MutableList<Version> = mutableListOf(),
        var showBenchmark: Boolean = false
) : KParcelable {

    fun hasAnyVersion(): Boolean {
        return versions.isNotEmpty()
    }

    fun hasDownloadingVersion(): Boolean {
        return versions
                .filter { it.isDownloading() }
                .isNotEmpty()
    }

    fun hasDownloadedVersion(): Boolean {
        return versions
                .filter { it.isDownloaded() }
                .isNotEmpty()
    }

    fun getLastVersion(): Version? {
        if (versions.isEmpty()) {
            return null
        }

        return versions
                .sortedBy { it.version }
                .lastOrNull()
    }

    fun getLastDownloadedVersion(): Version? {
        return versions
                .filter { it.isDownloaded() }
                .sortedBy { it.version }
                .lastOrNull()
    }

    fun hasUpdate(): Boolean {
        val lastDownloadVersion = getLastDownloadedVersion()?.version ?: 0
        if (lastDownloadVersion == 0) {
            return false
        }

        return versions
                .filter { it.isNotDownloaded() && it.version > lastDownloadVersion }
                .isNotEmpty()
    }

    private constructor(p: Parcel) : this(
            p.readString(),
            p.readString(),
            p.readString(),
            p.createStringArrayList(),
            ArrayList<Version>().apply { p.readList(this, Version::class.java.classLoader) },
            p.readBoolean()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeStringList(colors)
        dest.writeList(versions)
        dest.writeBoolean(showBenchmark)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (id == (other as Project).id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {

        @JvmField
        val CREATOR = parcelableCreator(::Project)
    }
}