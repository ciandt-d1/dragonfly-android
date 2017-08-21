package com.ciandt.dragonfly.example.data.remote.apis

import com.ciandt.dragonfly.example.data.remote.entities.Service
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ComparisonApi {

    @POST("/api/v1/other-services")
    @Headers(
            "Accept: application/json",
            "Content-Type: application/json"
    )
    fun compare(@Body body: CompareParams): Call<List<Service>>

    data class CompareParams(@SerializedName("value") @Expose var base64: String)
}