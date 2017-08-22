package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig
import okhttp3.internal.Version as HttpClientVersion

class Network {
    companion object {
        val USER_AGENT: String = "Dragonfly/Android/${BuildConfig.VERSION_NAME} ${HttpClientVersion.userAgent()}"
        val BASE_URL: String = BuildConfig.BASE_URL
    }
}