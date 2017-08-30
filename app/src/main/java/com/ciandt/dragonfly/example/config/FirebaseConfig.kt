package com.ciandt.dragonfly.example.config

/**
 * Created by iluz on 8/4/17.
 */
class FirebaseConfig {
    companion object {
        const val COLLECTION_FEEDBACK_STASH = "feedback_stash"

        const val SYNC_ITEMS_PER_RUN = 10

        const val REMOTE_CONFIG_CACHE_EXPIRATON = 3600L // 1 hour in seconds
    }
}