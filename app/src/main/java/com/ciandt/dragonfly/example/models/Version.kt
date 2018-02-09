package com.ciandt.dragonfly.example.models

import android.os.Parcel
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator
import java.io.Serializable

data class Version(
        var project: String = "",
        var version: Int = 0,
        var size: Long = 0L,
        var inputSize: Int = 0,
        var imageMean: Int = 0,
        var imageStd: Float = 0.0f,
        var inputName: String = "",
        var outputNames: String = "",
        var outputDisplayNames: String = "",
        var downloadUrl: String = "",
        var createdAt: Long = 0L,
        var modelPath: String = "",
        var labelFilesPaths: String = "",
        var status: Int = STATUS_NOT_DOWNLOADED
) : KParcelable {

    fun isNotDownloaded(): Boolean {
        return status == STATUS_NOT_DOWNLOADED
    }

    fun isDownloading(): Boolean {
        return status == STATUS_DOWNLOADING
    }

    fun isDownloaded(): Boolean {
        return status == STATUS_DOWNLOADED
    }

    fun toLibraryModel(others: HashMap<String, Serializable> = HashMap()): Model {
        val model = Model("$project/$version")
        model.version = version
        model.modelPath = modelPath
        model.setLabelFilesPaths(labelFilesPaths)
        model.sizeInBytes = size
        model.inputSize = inputSize
        model.imageMean = imageMean
        model.imageStd = imageStd
        model.inputName = inputName
        model.setOutputNames(outputNames)
        model.setOutputDisplayNames(outputDisplayNames)
        model.others = others
        return model
    }

    private constructor(p: Parcel) : this(
            p.readString(),
            p.readInt(),
            p.readLong(),
            p.readInt(),
            p.readInt(),
            p.readFloat(),
            p.readString(),
            p.readString(),
            p.readString(),
            p.readString(),
            p.readLong(),
            p.readString(),
            p.readString(),
            p.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(project)
        writeInt(version)
        writeLong(size)
        writeInt(inputSize)
        writeInt(imageMean)
        writeFloat(imageStd)
        writeString(inputName)
        writeString(outputNames)
        writeString(outputDisplayNames)
        writeString(downloadUrl)
        writeLong(createdAt)
        writeString(modelPath)
        writeString(labelFilesPaths)
        writeInt(status)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) {
            return false
        }
        if (project != other.project) return false
        return (version == other.version)
    }

    companion object {

        val STATUS_NOT_DOWNLOADED = 0
        val STATUS_DOWNLOADING = 1
        val STATUS_DOWNLOADED = 2

        @JvmField
        val CREATOR = parcelableCreator(::Version)
    }
}