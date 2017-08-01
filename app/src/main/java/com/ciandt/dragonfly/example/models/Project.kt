package com.ciandt.dragonfly.example.models

import android.os.Parcel
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.data.mapper.ProjectToLibraryModelMapper
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator

data class Project(
        var id: String = "",
        var name: String = "",
        var description: String = "",
        var colors: List<String> = emptyList(),
        var versions: List<Version> = emptyList()
) : KParcelable {

    fun hasAnyVersion(): Boolean = versions.isNotEmpty()

    val lastVersion: Version?
        get() {
            if (versions.isEmpty()) {
                return null
            } else {
                return versions.last()
            }
        }

    var status: Int
        get() {
            return lastVersion?.status ?: 0
        }
        set(value) {
            lastVersion?.status = value
        }

    fun isDownloading(): Boolean {
        return lastVersion?.isDownloading() ?: false
    }

    fun isDownloaded(): Boolean {
        return lastVersion?.isDownloaded() ?: false
    }

    fun toLibraryModel(): Model {
        return ProjectToLibraryModelMapper(this).map()
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

        @JvmField val CREATOR = parcelableCreator(::Project)
    }
}