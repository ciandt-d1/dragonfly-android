package com.ciandt.dragonfly.example.data.remote.interceptors

import com.ciandt.dragonfly.example.config.Network
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val newRequest = chain.request().newBuilder()
                .header("User-Agent", Network.USER_AGENT)
                .build()

        return chain.proceed(newRequest)
    }
}