package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig

/**
 * Created by iluz on 5/30/17.
 */
interface PreferenceKeys {
    companion object {
        val REAL_TIME_PERMISSIONS_PERMANENTLY_DENIED = "${BuildConfig.APPLICATION_ID}.real_time_permissions_permanently_denied"
    }
}
