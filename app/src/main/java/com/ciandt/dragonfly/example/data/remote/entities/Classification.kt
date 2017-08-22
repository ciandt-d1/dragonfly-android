package com.ciandt.dragonfly.example.data.remote.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Classification(

        @SerializedName("label")
        @Expose
        var label: String? = null,

        @SerializedName("score")
        @Expose
        var score: Float = 0.toFloat()
)