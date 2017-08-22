package com.ciandt.dragonfly.example.data.remote

import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.data.remote.apis.BenchmarkApi
import com.ciandt.dragonfly.example.data.remote.entities.BenchmarkService
import com.ciandt.dragonfly.example.data.remote.interceptors.AuthorizationInterceptor
import com.ciandt.dragonfly.example.data.remote.interceptors.UserAgentInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RemoteDataSource(private val baseUrl: String) {

    private val httpClient by lazy {
        OkHttpClient.Builder().apply {

            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)

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

    private val benchmarkApi by lazy {
        retrofit.create(BenchmarkApi::class.java)
    }

    fun benchmark(image: String): List<BenchmarkService> {
        val request = benchmarkApi.benchmark(BenchmarkApi.BenchmarkParams(image))
        val response = request.execute()

        if (response.isSuccessful) {
            return response.body()!!
        }

        throw RuntimeException("Response Code: ${response.code()}\n${response.errorBody()}")
    }
}