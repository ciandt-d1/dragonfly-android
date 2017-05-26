package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig

/**
 * Created by iluz on 5/15/17.
 */

interface Features {
    companion object {

        val ENABLE_DEBUG_DRAWER = BuildConfig.DEBUG && true
    }
}
