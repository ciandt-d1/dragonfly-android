package com.ciandt.dragonfly.example.features.feedback.model

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

/**
 * Created by iluz on 6/23/17.
 */

data class Feedback(
        val tenant: String,
        val project: String,
        val userId: String,
        val modelVersion: Int,
        val value: Int,
        val actualLabel: String,
        val identifiedLabels: Map<String, Float>,
        val imageLocalPath: String,
        val imageGcsPath: String? = null,
        val uploadToGcsFinished: Boolean = false,
        val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    @Exclude
    fun isPositive() = value == POSITIVE

    @Exclude
    fun isNegative() = value == NEGATIVE

    override fun toString(): String {
        return "Feedback(tenant='$tenant', project='$project', userId='$userId', modelVersion=$modelVersion, value=$value, actualLabel='$actualLabel', identifiedLabels=$identifiedLabels, imageLocalPath='$imageLocalPath', imageGcsPath=$imageGcsPath, uploadToGcsFinished=$uploadToGcsFinished, createdAt=$createdAt)"
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
            source.readLong()
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
    }
}