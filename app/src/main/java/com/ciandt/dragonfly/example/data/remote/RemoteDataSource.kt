package com.ciandt.dragonfly.example.data.remote

import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.data.remote.apis.ComparisonApi
import com.ciandt.dragonfly.example.data.remote.entities.Service
import com.ciandt.dragonfly.example.data.remote.interceptors.AuthorizationInterceptor
import com.ciandt.dragonfly.example.data.remote.interceptors.UserAgentInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RemoteDataSource(private val baseUrl: String) {

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
            addInterceptor(UserAgentInterceptor())
            addInterceptor(AuthorizationInterceptor())
        }.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .client(httpClient)
                .build()
    }

    private val comparisonApi by lazy {
        retrofit.create(ComparisonApi::class.java)
    }

    fun compareServices(image: String): List<Service> {
        val request = comparisonApi.compare()
        val response = request.execute()

        if (response.isSuccessful) {
            return response.body()!!
        }

        throw RuntimeException("Response Code: ${response.code()}\n${response.errorBody()}")
    }
}