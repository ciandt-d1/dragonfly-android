package com.ciandt.dragonfly.example.config

import com.ciandt.dragonfly.example.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * Created by iluz on 5/15/17.
 */

interface Features {
    companion object {

        val ENABLE_DEBUG_DRAWER = BuildConfig.DEBUG && true
        val SAVE_CAPTURED_IMAGES_FOR_DEBUGGING = false
        val SAVE_SELECTED_IMAGE_FOR_DEBUGGING = false
        val SHOW_PROJECTS_WITHOUT_VERSIONS = FirebaseRemoteConfig.getInstance().getBoolean("show_projects_without_versions")
    }
}
