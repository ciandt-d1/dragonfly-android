package com.ciandt.dragonfly.example.models

import android.os.Parcel
import android.os.Parcelable
import com.ciandt.dragonfly.data.model.Model
import com.ciandt.dragonfly.example.shared.KParcelable
import com.ciandt.dragonfly.example.shared.parcelableCreator

data class Version(
        var project: String = "",
        var version: Int = 0,
        var size: Long = 0L,
        var inputSize: Int = 0,
        var imageMean: Int = 0,
        var imageStd: Float = 0.0f,
        var inputName: String = "",
        var outputName: String = "",
        var downloadUrl: String = "",
        var createdAt: Long = 0L,
        var modelPath: String = "",
        var labelsPath: String = "",
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

    fun toLibraryModel(others: Map<String, Parcelable> = emptyMap()): Model {
        val model = Model("$project/$version")
        model.version = version
        model.modelPath = modelPath
        model.labelsPath = labelsPath
        model.inputSize = inputSize
        model.imageMean = imageMean
        model.imageStd = imageStd
        model.inputName = inputName
        model.outputName = outputName
        model.modelPath = modelPath
        model.labelsPath = labelsPath
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
        writeString(outputName)
        writeString(downloadUrl)
        writeLong(createdAt)
        writeString(modelPath)
        writeString(labelsPath)
        writeInt(status)
    }

    companion object {

        val STATUS_NOT_DOWNLOADED = 0
        val STATUS_DOWNLOADING = 1
        val STATUS_DOWNLOADED = 2

        @JvmField val CREATOR = parcelableCreator(::Version)
    }
}