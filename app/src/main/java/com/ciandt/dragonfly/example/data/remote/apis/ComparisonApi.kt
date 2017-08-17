package com.ciandt.dragonfly.example.data.remote.apis

import com.ciandt.dragonfly.example.data.remote.entities.Service
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST

interface ComparisonApi {

    @POST("/api/v1/other-services?mock=true")
    @Headers("Accept: application/json")
    fun compare(): Call<List<Service>>
}