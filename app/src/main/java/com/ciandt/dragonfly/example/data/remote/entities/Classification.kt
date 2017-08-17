package com.ciandt.dragonfly.example.data.remote.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Classification(

        @SerializedName("label")
        @Expose
        var label: String? = null,

        @SerializedName("confidence")
        @Expose
        var confidence: Double = 0.toDouble()
)