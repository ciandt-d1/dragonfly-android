package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig

/**
 * Created by iluz on 5/30/17.
 */
interface PreferenceKeys {
    companion object {
        val REAL_TIME_PERMISSION_PERMANENTLY_DENIED = String.format("%s.real_time_permissions_permanently_denied", BuildConfig.APPLICATION_ID);
    }
}
