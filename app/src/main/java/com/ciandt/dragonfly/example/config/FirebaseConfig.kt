package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig
import com.ciandt.dragonfly.example.infrastructure.extensions.hoursToSeconds
import com.ciandt.dragonfly.example.infrastructure.extensions.minutesToSeconds

/**
 * Created by iluz on 8/4/17.
 */
class FirebaseConfig {
    companion object {
        const val COLLECTION_FEEDBACK_STASH = "feedback_stash"

        const val SYNC_ITEMS_PER_RUN = 10

        val REMOTE_CONFIG_CACHE_EXPIRATON = if (BuildConfig.DEBUG) 1.minutesToSeconds() else 1.hoursToSeconds()
    }
}