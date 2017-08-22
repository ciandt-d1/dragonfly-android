package com.ciandt.dragonfly.example.data.remote.apis

import com.ciandt.dragonfly.example.data.remote.entities.BenchmarkService
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface BenchmarkApi {

    @POST("/api/v1/benchmark")
    @Headers(
            "Accept: application/json",
            "Content-Type: application/json"
    )
    fun benchmark(@Body body: BenchmarkParams): Call<List<BenchmarkService>>

    data class BenchmarkParams(@SerializedName("value") @Expose var base64: String)
}