package com.ciandt.dragonfly.example.data.remote.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class BenchmarkService(

        @SerializedName("serviceId")
        @Expose
        var id: String? = null,

        @SerializedName("serviceName")
        @Expose
        var name: String? = null,

        @SerializedName("classifications")
        @Expose
        var classifications: List<Classification> = ArrayList()
)