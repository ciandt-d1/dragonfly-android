package com.ciandt.dragonfly.example.models

import android.os.Parcel
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
        var labelPath: String = "",
        var status: Int = 0
) : KParcelable {

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
        writeString(labelPath)
        writeInt(status)
    }

    companion object {

        @JvmField val CREATOR = parcelableCreator(::Version)
    }
}