package com.ciandt.dragonfly.example.features.feedback.model

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by iluz on 6/23/17.
 */
@IgnoreExtraProperties
data class Feedback(
        var tenant: String = "",
        var project: String = "",
        var userId: String = "",
        var modelVersion: Int = 0,
        var value: Int = 0,
        var actualLabel: String = "",
        var identifiedLabels: Map<String, Float> = mapOf(),
        var imageLocalPath: String = "",
        var imageGcsPath: String? = null,
        var uploadToGcsFinished: Boolean = false,
        var createdAt: Long = System.currentTimeMillis(),
        var tenantUserProject: String? = null
) : Parcelable {

    @Exclude
    fun isPositive() = value == POSITIVE

    @Exclude
    fun isNegative() = value == NEGATIVE

    override fun toString(): String {
        return "Feedback(tenant='$tenant', project='$project', userId='$userId', modelVersion=$modelVersion, value=$value, actualLabel='$actualLabel', identifiedLabels=$identifiedLabels, imageLocalPath='$imageLocalPath', imageGcsPath=$imageGcsPath, uploadToGcsFinished=$uploadToGcsFinished, createdAt=$createdAt, tenantUserProject=$tenantUserProject)"
    }

    companion object {
        val POSITIVE = 1

        val NEGATIVE = 0

        @JvmField val CREATOR: Parcelable.Creator<Feedback> = object : Parcelable.Creator<Feedback> {
            override fun createFromParcel(source: Parcel): Feedback = Feedback(source)
            override fun newArray(size: Int): Array<Feedback?> = arrayOfNulls(size)
        }

        private fun identifiedLabelsBundleToMap(bundle: Bundle): Map<String, Float> {
            val map = HashMap<String, Float>()

            for (key in bundle.keySet()) {
                map.put(key, bundle.getFloat(key))
            }

            return map
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            identifiedLabelsBundleToMap(source.readBundle()),
            source.readString(),
            source.readString(),
            1 == source.readInt(),
            source.readLong(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(tenant)
        dest.writeString(project)
        dest.writeString(userId)
        dest.writeInt(modelVersion)
        dest.writeInt(value)
        dest.writeString(actualLabel)

        val identifiedLabelsBundle = Bundle()
        identifiedLabels.keys.forEach { key ->
            identifiedLabelsBundle.putFloat(key, identifiedLabels[key]!!)
        }
        dest.writeBundle(identifiedLabelsBundle)

        dest.writeString(imageLocalPath)
        dest.writeString(imageGcsPath)
        dest.writeInt((if (uploadToGcsFinished) 1 else 0))
        dest.writeLong(createdAt)
        dest.writeString(tenantUserProject)
    }
}