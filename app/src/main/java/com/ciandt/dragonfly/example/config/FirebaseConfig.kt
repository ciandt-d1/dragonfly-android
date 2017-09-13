package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig

/**
 * Created by iluz on 8/4/17.
 */
class FirebaseConfig {
    companion object {
        const val COLLECTION_FEEDBACK_STASH = "feedback_stash"

        const val SYNC_ITEMS_PER_RUN = 10

        val REMOTE_CONFIG_CACHE_EXPIRATON = if (BuildConfig.DEBUG) 60L else 3600L // 3600 = 1 hour in seconds
    }
}