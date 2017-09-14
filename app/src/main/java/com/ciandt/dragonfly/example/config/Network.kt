package com.ciandt.dragonfly.example.config

import android.os.Build
import com.ciandt.dragonfly.example.BuildConfig
import okhttp3.internal.Version as HttpClientVersion

class Network {
    companion object {
        val USER_AGENT = "Dragonfly ${BuildConfig.VERSION_NAME}/Android ${Build.VERSION.RELEASE} ${Build.MANUFACTURER} ${Build.MODEL}/OkHttpAgent: ${HttpClientVersion.userAgent().replace("/", "-")}"
        val BASE_URL = BuildConfig.BASE_URL
    }
}